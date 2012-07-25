/*
 * Copyright (c) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.api.client.googleapis.media;

import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.ExponentialBackOffPolicy;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.common.base.Preconditions;

import java.io.OutputStream;

/**
 * Media HTTP Downloader, with support for both direct and resumable media downloads. Documentation
 * is available <a
 * href='http://code.google.com/p/google-api-java-client/wiki/MediaDownload'>here</a>.
 *
 * <p>
 * Implementation is not thread-safe.
 * </p>
 *
 * @since 1.9
 *
 * @author rmistry@google.com (Ravi Mistry)
 */
public final class MediaHttpDownloader {

  /**
   * Download state associated with the Media HTTP downloader.
   */
  public enum DownloadState {
    /** The download process has not started yet. */
    NOT_STARTED,

    /** Set after a media file chunk is downloaded. */
    MEDIA_IN_PROGRESS,

    /** Set after the complete media file is successfully downloaded. */
    MEDIA_COMPLETE
  }

  /**
   * Default maximum number of bytes that will be downloaded from the server in any single HTTP
   * request. Set to 32MB because that is the maximum App Engine request size.
   */
  public static final int MAXIMUM_CHUNK_SIZE = 32 * MediaHttpUploader.MB;

  /** The request factory for connections to the server. */
  private final HttpRequestFactory requestFactory;

  /** The transport to use for requests. */
  private final HttpTransport transport;

  /**
   * Determines whether the back off policy is enabled or disabled. If value is set to {@code false}
   * then server errors are not handled and the download process will fail if a server error is
   * encountered. Defaults to {@code true}.
   */
  private boolean backOffPolicyEnabled = true;

  /**
   * Determines whether direct media download is enabled or disabled. If value is set to
   * {@code true} then a direct download will be done where the whole media content is downloaded in
   * a single request. If value is set to {@code false} then the download uses the resumable media
   * download protocol to download in data chunks. Defaults to {@code false}.
   */
  private boolean directDownloadEnabled = false;

  /**
   * Progress listener to send progress notifications to or {@code null} for none.
   */
  private MediaHttpDownloaderProgressListener progressListener;

  /**
   * Maximum size of individual chunks that will get downloaded by single HTTP requests. The default
   * value is {@link #MAXIMUM_CHUNK_SIZE}.
   */
  private int chunkSize = MAXIMUM_CHUNK_SIZE;

  /**
   * The length of the HTTP media content or {@code 0} before it is initialized in
   * {@link #setMediaContentLength}.
   */
  private long mediaContentLength;

  /** The current state of the downloader. */
  private DownloadState downloadState = DownloadState.NOT_STARTED;

  /** The total number of bytes downloaded by this downloader. */
  private long bytesDownloaded;

  /**
   * Construct the {@link MediaHttpDownloader}.
   *
   * @param transport The transport to use for requests
   * @param httpRequestInitializer The initializer to use when creating an {@link HttpRequest} or
   *        {@code null} for none
   */
  public MediaHttpDownloader(HttpTransport transport,
      HttpRequestInitializer httpRequestInitializer) {
    this.transport = Preconditions.checkNotNull(transport);
    this.requestFactory =
        httpRequestInitializer == null ? transport.createRequestFactory() : transport
            .createRequestFactory(httpRequestInitializer);
  }

  /**
   * Executes a direct media download or a resumable media download.
   *
   * <p>
   * This method does not close the given output stream.
   * </p>
   *
   * <p>
   * This method is not reentrant. A new instance of {@link MediaHttpDownloader} must be
   * instantiated before download called be called again.
   * </p>
   *
   * <p>
   * Upgrade warning: this method now throws an {@link Exception}. In prior version 1.10 it threw
   * an {@link java.io.IOException}.
   * </p>
   *
   * @param requestUrl The request URL where the download requests will be sent
   * @param outputStream destination output stream
   */
  public void download(GenericUrl requestUrl, OutputStream outputStream) throws Exception {
    Preconditions.checkArgument(downloadState == DownloadState.NOT_STARTED);
    requestUrl.put("alt", "media");

    if (directDownloadEnabled) {
      updateStateAndNotifyListener(DownloadState.MEDIA_IN_PROGRESS);

      HttpRequest request = requestFactory.buildGetRequest(requestUrl);
      HttpResponse response = request.execute();

      try {
        // All required bytes have been downloaded from the server.
        mediaContentLength = response.getHeaders().getContentLength();
        bytesDownloaded = mediaContentLength;
        updateStateAndNotifyListener(DownloadState.MEDIA_COMPLETE);
        AbstractInputStreamContent.copy(response.getContent(), outputStream);
      } finally {
        response.disconnect();
      }
      return;
    }

    // Download the media content in chunks.
    while (true) {
      HttpRequest request = requestFactory.buildGetRequest(requestUrl);
      request.getHeaders().setRange(
          "bytes=" + bytesDownloaded + "-" + (bytesDownloaded + chunkSize - 1));

      if (backOffPolicyEnabled) {
        // Set ExponentialBackOffPolicy as the BackOffPolicy of the HTTP Request which will
        // retry the same request again if there is a server error.
        request.setBackOffPolicy(new ExponentialBackOffPolicy());
      }

      HttpResponse response = request.execute();
      AbstractInputStreamContent.copy(response.getContent(), outputStream);

      String contentRange = response.getHeaders().getContentRange();
      long nextByteIndex = getNextByteIndex(contentRange);
      setMediaContentLength(contentRange);

      if (mediaContentLength <= nextByteIndex) {
        // All required bytes have been downloaded from the server.
        bytesDownloaded = mediaContentLength;
        updateStateAndNotifyListener(DownloadState.MEDIA_COMPLETE);
        return;
      }

      bytesDownloaded = nextByteIndex;
      updateStateAndNotifyListener(DownloadState.MEDIA_IN_PROGRESS);
    }
  }

  /**
   * Returns the next byte index identifying data that the server has not yet sent out, obtained
   * from the HTTP Content-Range header (E.g a header of "Content-Range: 0-55/1000" would cause 56
   * to be returned). <code>null</code> headers cause 0 to be returned.
   *
   * @param rangeHeader in the HTTP response
   * @return the byte index beginning where the server has yet to send out data
   */
  private long getNextByteIndex(String rangeHeader) {
    if (rangeHeader == null) {
      return 0L;
    }
    return Long.parseLong(rangeHeader.substring(rangeHeader.indexOf('-') + 1,
        rangeHeader.indexOf('/'))) + 1;
  }

  /**
   * Sets the total number of bytes that have been downloaded of the media resource.
   *
   * <p>
   * If a download was aborted mid-way due to a connection failure then users can resume the
   * download from the point where it left off.
   * </p>
   *
   * <p>
   * This method is only applicable for resumable media download.
   * </p>
   *
   * @param bytesDownloaded The total number of bytes downloaded
   */
  public MediaHttpDownloader setBytesDownloaded(long bytesDownloaded) {
    this.bytesDownloaded = bytesDownloaded;
    return this;
  }

  /**
   * Sets the media content length from the HTTP Content-Range header (E.g a header of
   * "Content-Range: 0-55/1000" would cause 1000 to be set. <code>null</code> headers do not set
   * anything.
   *
   * @param rangeHeader in the HTTP response
   */
  private void setMediaContentLength(String rangeHeader) {
    if (rangeHeader == null) {
      return;
    }
    if (mediaContentLength == 0) {
      mediaContentLength = Long.parseLong(rangeHeader.substring(rangeHeader.indexOf('/') + 1));
    }
  }

  /**
   * Returns whether direct media download is enabled or disabled. If value is set to {@code true}
   * then a direct download will be done where the whole media content is downloaded in a single
   * request. If value is set to {@code false} then the download uses the resumable media download
   * protocol to download in data chunks. Defaults to {@code false}.
   */
  public boolean isDirectDownloadEnabled() {
    return directDownloadEnabled;
  }

  /**
   * Returns whether direct media download is enabled or disabled. If value is set to {@code true}
   * then a direct download will be done where the whole media content is downloaded in a single
   * request. If value is set to {@code false} then the download uses the resumable media download
   * protocol to download in data chunks. Defaults to {@code false}.
   */
  public MediaHttpDownloader setDirectDownloadEnabled(boolean directDownloadEnabled) {
    this.directDownloadEnabled = directDownloadEnabled;
    return this;
  }

  /**
   * Sets the progress listener to send progress notifications to or {@code null} for none.
   */
  public MediaHttpDownloader setProgressListener(
      MediaHttpDownloaderProgressListener progressListener) {
    this.progressListener = progressListener;
    return this;
  }

  /**
   * Returns the progress listener to send progress notifications to or {@code null} for none.
   */
  public MediaHttpDownloaderProgressListener getProgressListener() {
    return progressListener;
  }

  /**
   * Sets whether the back off policy is enabled or disabled. If value is set to {@code false} then
   * server errors are not handled and the download process will fail if a server error is
   * encountered. Defaults to {@code true}.
   */
  public MediaHttpDownloader setBackOffPolicyEnabled(boolean backOffPolicyEnabled) {
    this.backOffPolicyEnabled = backOffPolicyEnabled;
    return this;
  }

  /**
   * Returns whether the back off policy is enabled or disabled. If value is set to {@code false}
   * then server errors are not handled and the download process will fail if a server error is
   * encountered. Defaults to {@code true}.
   */
  public boolean isBackOffPolicyEnabled() {
    return backOffPolicyEnabled;
  }

  /** Returns the transport to use for requests. */
  public HttpTransport getTransport() {
    return transport;
  }

  /**
   * Sets the maximum size of individual chunks that will get downloaded by single HTTP requests.
   * The default value is {@link #MAXIMUM_CHUNK_SIZE}.
   *
   * <p>
   * The maximum allowable value is {@link #MAXIMUM_CHUNK_SIZE}.
   * </p>
   */
  public MediaHttpDownloader setChunkSize(int chunkSize) {
    Preconditions.checkArgument(chunkSize > 0 && chunkSize <= MAXIMUM_CHUNK_SIZE);
    this.chunkSize = chunkSize;
    return this;
  }

  /**
   * Returns the maximum size of individual chunks that will get downloaded by single HTTP requests.
   * The default value is {@link #MAXIMUM_CHUNK_SIZE}.
   */
  public int getChunkSize() {
    return chunkSize;
  }

  /**
   * Gets the total number of bytes downloaded by this downloader.
   *
   * @return the number of bytes downloaded
   */
  public long getNumBytesDownloaded() {
    return bytesDownloaded;
  }

  /**
   * Sets the download state and notifies the progress listener.
   *
   * <p>
   * Upgrade warning: this method now throws an {@link Exception}. In prior version 1.10 it threw
   * an {@link java.io.IOException}.
   * </p>
   *
   * @param downloadState value to set to
   */
  private void updateStateAndNotifyListener(DownloadState downloadState) throws Exception {
    this.downloadState = downloadState;
    if (progressListener != null) {
      progressListener.progressChanged(this);
    }
  }

  /**
   * Gets the current download state of the downloader.
   *
   * @return the download state
   */
  public DownloadState getDownloadState() {
    return downloadState;
  }

  /**
   * Gets the download progress denoting the percentage of bytes that have been downloaded,
   * represented between 0.0 (0%) and 1.0 (100%).
   *
   * @return the download progress
   */
  public double getProgress() {
    return mediaContentLength == 0 ? 0 : (double) bytesDownloaded / mediaContentLength;
  }
}
