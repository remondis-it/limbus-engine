package com.remondis.limbus.engine.logging;

import java.io.FilterOutputStream;

//
//  ========================================================================
//  Copyright (c) 1995-2017 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.remondis.limbus.files.FileAccessException;
import com.remondis.limbus.files.LimbusFileService;
import com.remondis.limbus.utils.Lang;

/**
 * RolloverFileOutputStream.
 *
 * <p>
 * This output stream puts content in a file that is rolled over every 24 hours.
 * The filename must include the string "yyyy_mm_dd", which is replaced with the
 * actual date when creating and rolling over the file.
 * </p>
 * <p>
 * Old files are retained for a number of days before being deleted.
 * </p>
 */
public class RolloverFileOutputStream extends FilterOutputStream {

  private static final Logger log = LoggerFactory.getLogger(RolloverFileOutputStream.class);

  private static final String LOGGING_EXTENSION = "out";

  private final static AtomicInteger instances = new AtomicInteger();

  private static Timer __rollover;

  final static String YYYY_MM_DD = "yyyy_mm_dd";
  final static String ROLLOVER_FILE_DATE_FORMAT = "yyyy_MM_dd";
  final static String ROLLOVER_FILE_BACKUP_FORMAT = "HHmmssSSS";

  private RollTask _rollTask;
  private ZonedDateTime midnight;
  private SimpleDateFormat _fileBackupFormat;
  private SimpleDateFormat _fileDateFormat;

  private String _filenamePattern;
  private String _filename;
  private boolean _append;

  private LimbusFileService fileService;

  /* ------------------------------------------------------------ */
  /**
   * @param fileService
   *        The file service.
   * @param filename
   *        The filename must include the string "yyyy_mm_dd",
   *        which is replaced with the actual date when creating and rolling over the file.
   * @throws FileAccessException
   *         if unable to create output
   */
  public RolloverFileOutputStream(LimbusFileService fileService, String filename) throws FileAccessException {
    this(fileService, filename, true);
  }

  /* ------------------------------------------------------------ */
  /**
   * @param fileService
   *        The file service.
   * @param filename
   *        The filename must include the string "yyyy_mm_dd",
   *        which is replaced with the actual date when creating and rolling over the file.
   * @param append
   *        If true, existing files will be appended to.
   * @throws FileAccessException
   *         if unable to create output
   */
  public RolloverFileOutputStream(LimbusFileService fileService, String filename, boolean append)
      throws FileAccessException {
    this(fileService, filename, append, TimeZone.getDefault());
  }

  /* ------------------------------------------------------------ */
  /**
   * @param fileService
   *        The file service.
   * @param filename
   *        The filename must include the string "yyyy_mm_dd",
   *        which is replaced with the actual date when creating and rolling over the file.
   * @param append
   *        If true, existing files will be appended to.
   * @param zone
   *        the timezone for the output
   * @throws FileAccessException
   *         if unable to create output
   */
  public RolloverFileOutputStream(LimbusFileService fileService, String filename, boolean append, TimeZone zone)
      throws FileAccessException {

    this(fileService, filename, append, zone, null, null);
  }

  /* ------------------------------------------------------------ */
  /**
   * @param fileService
   *        The file service.
   * @param filename
   *        The filename must include the string "yyyy_mm_dd",
   *        which is replaced with the actual date when creating and rolling over the file.
   * @param append
   *        If true, existing files will be appended to.
   * @param zone
   *        the timezone for the output
   * @param dateFormat
   *        The format for the date file substitution. The default is "yyyy_MM_dd".
   * @param backupFormat
   *        The format for the file extension of backup files. The default is "HHmmssSSS".
   * @throws FileAccessException
   *         if unable to create output
   */
  public RolloverFileOutputStream(LimbusFileService fileService, String filename, boolean append, TimeZone zone,
      String dateFormat, String backupFormat) throws FileAccessException {
    super(null);
    Lang.denyNull("fileService", fileService);
    Lang.denyNull("filename", filename);
    Lang.denyNull("timezone", zone);

    this.fileService = fileService;

    if (dateFormat == null)
      dateFormat = ROLLOVER_FILE_DATE_FORMAT;
    _fileDateFormat = new SimpleDateFormat(dateFormat);

    if (backupFormat == null)
      backupFormat = ROLLOVER_FILE_BACKUP_FORMAT;
    _fileBackupFormat = new SimpleDateFormat(backupFormat);

    _fileBackupFormat.setTimeZone(zone);
    _fileDateFormat.setTimeZone(zone);

    if (filename != null) {
      filename = filename.trim();
      if (filename.length() == 0)
        filename = null;
    }
    if (filename == null)
      throw new IllegalArgumentException("Invalid filename");

    _filenamePattern = filename;
    _append = append;
    setFile();

    synchronized (RolloverFileOutputStream.class) {
      if (__rollover == null)
        __rollover = new Timer(RolloverFileOutputStream.class.getName(), true);

      _rollTask = new RollTask();

      midnight = toMidnight(ZonedDateTime.now(), zone.toZoneId());

      scheduleNextRollover();
      instances.incrementAndGet();
    }

  }

  /**
   * Get the "start of day" for the provided DateTime at the zone specified.
   *
   * @param dateTime
   *        the date time to calculate from
   * @param zone
   *        the zone to return the date in
   * @return start of the day of the date provided
   */
  public static ZonedDateTime toMidnight(ZonedDateTime dateTime, ZoneId zone) {
    return dateTime.toLocalDate()
        .atStartOfDay(zone);
  }

  /**
   * Get the next "start of day" for the provided date.
   *
   * @param dateTime
   *        the date to calculate from
   * @return the start of the next day
   */
  public static ZonedDateTime nextMidnight(ZonedDateTime dateTime) {
    // Increment to next day.
    // Using Calendar.add(DAY, 1) takes in account Daylights Savings
    // differences, and still maintains the "midnight" settings for
    // Hour, Minute, Second, Milliseconds
    return dateTime.toLocalDate()
        .plus(1, ChronoUnit.DAYS)
        .atStartOfDay(dateTime.getZone());
  }

  private void scheduleNextRollover() {
    synchronized (RolloverFileOutputStream.class) {
      midnight = nextMidnight(midnight);
      long delay = midnight.toInstant()
          .toEpochMilli() - System.currentTimeMillis();
      _rollTask = new RollTask();
      __rollover.schedule(_rollTask, delay);
    }
  }

  /* ------------------------------------------------------------ */
  public String getCurrentFilename() {
    return getLogFilepath(getLogFile(_filename));
  }

  /* ------------------------------------------------------------ */
  private synchronized void setFile() throws FileAccessException {
    Date now = new Date();

    // Is this a rollover file?
    String filename = _filenamePattern;
    int i = filename.toLowerCase(Locale.ENGLISH)
        .indexOf(YYYY_MM_DD);
    if (i >= 0) {
      filename = filename.substring(0, i) + _fileDateFormat.format(now) + filename.substring(i + YYYY_MM_DD.length());
    }

    // Do we need to change the output stream?
    if (out == null || !filename.equals(_filename)) {
      // Yep
      _filename = filename;
      if (!_append && fileService.hasFile(getLogFilepath(getLogFile(filename))))
        fileService.renameFile(getLogFilepath(getLogFile(filename)),
            getLogFile(filename) + "." + _fileBackupFormat.format(now));
      OutputStream oldOut = out;
      out = fileService.createFile(getLogFilepath(getLogFile(filename)), _append);
      if (oldOut != null)
        try {
          oldOut.close();
        } catch (IOException e) {
          // Keep this silent.
        }
    }
  }

  protected String getLogFile(String filename) {
    return filename + "." + LOGGING_EXTENSION;
  }

  protected String getLogFilepath(String filename) {
    return fileService.toPath(LimbusFileService.LOGGING_DIRECTORY, filename);
  }

  /* ------------------------------------------------------------ */
  @Override
  public void write(byte[] buf) throws IOException {
    out.write(buf);
  }

  /* ------------------------------------------------------------ */
  @Override
  public void write(byte[] buf, int off, int len) throws IOException {
    out.write(buf, off, len);
  }

  /* ------------------------------------------------------------ */
  @Override
  public void close() throws IOException {
    synchronized (RolloverFileOutputStream.class) {
      try {
        try {
          super.close();
        } finally {
          out = null;
          _filename = null;
        }
        _rollTask.cancel();
      } finally {
        int instanceCount = instances.decrementAndGet();
        if (instanceCount == 0) {
          __rollover.cancel();
          __rollover = null;
        }
      }
    }
  }

  /* ------------------------------------------------------------ */
  private class RollTask extends TimerTask {
    @Override
    public void run() {
      try {
        RolloverFileOutputStream.this.setFile();
        RolloverFileOutputStream.this.scheduleNextRollover();
      } catch (Exception e) {
        // Cannot log this exception to a LOG, as RolloverFOS can be used by logging
        log.warn("Error whil rolling plugin output logging file.", e);
      }
    }
  }
}
