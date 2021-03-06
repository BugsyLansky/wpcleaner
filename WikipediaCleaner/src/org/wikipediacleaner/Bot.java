/*
 *  WPCleaner: A tool to help on Wikipedia maintenance tasks.
 *  Copyright (C) 2013  Nicolas Vervelle
 *
 *  See README.txt file for licensing information.
 */

package org.wikipediacleaner;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wikipediacleaner.api.check.algorithm.CheckErrorAlgorithm;
import org.wikipediacleaner.api.check.algorithm.CheckErrorAlgorithms;
import org.wikipediacleaner.api.constants.EnumLanguage;
import org.wikipediacleaner.api.constants.EnumWikipedia;
import org.wikipediacleaner.api.data.ISBNRange;
import org.wikipediacleaner.gui.swing.basic.BasicWorker;
import org.wikipediacleaner.gui.swing.basic.BasicWorkerListener;
import org.wikipediacleaner.gui.swing.bot.AutomaticCWWorker;
import org.wikipediacleaner.gui.swing.bot.ListCWWorker;
import org.wikipediacleaner.gui.swing.worker.LoginWorker;
import org.wikipediacleaner.gui.swing.worker.UpdateDabWarningWorker;
import org.wikipediacleaner.gui.swing.worker.UpdateDuplicateArgsWarningWorker;
import org.wikipediacleaner.gui.swing.worker.UpdateISBNWarningWorker;
import org.wikipediacleaner.gui.swing.worker.UpdateISSNWarningWorker;
import org.wikipediacleaner.i18n.GT;
import org.wikipediacleaner.utils.Configuration;
import org.wikipediacleaner.utils.ConfigurationConstants;
import org.wikipediacleaner.utils.ConfigurationValueBoolean;
import org.wikipediacleaner.utils.ConfigurationValueString;


/**
 * Wikipedia Cleaner running as a bot.
 */
public class Bot implements BasicWorkerListener {

  private final static Log log = LogFactory.getLog(Bot.class);

  /**
   * @param args
   */
  public static void main(String[] args) {
    log.info("Running as bot");

    // Log levels
    Logger.getLogger("org.lobobrowser").setLevel(Level.WARNING);
    Logger.getLogger("").setLevel(Level.WARNING);

    Configuration config = Configuration.getConfiguration();
    EnumLanguage language = config.getLanguage();
    Locale.setDefault(language.getLocale());

    // Debugging
    if (config.getBoolean(null, ConfigurationValueBoolean.DEBUG_DETAILS)) {
      Logger.getLogger("org.wikipediacleaner").setLevel(Level.FINE);
    }
    if (config.getBoolean(null, ConfigurationValueBoolean.DEBUG_FILE)) {
      try {
        Handler fh = new FileHandler("WPCleanerBot.log");
        fh.setFormatter(new SimpleFormatter());
        Logger.getLogger("").addHandler(fh);
      } catch (Exception e) {
        // Nothing to do
      }
    }

    // Language
    GT.setCurrentLanguage(config.getLanguage());

    // Various initializations
    ISBNRange.initialize();

    new Bot(args);
  }

  /** Time limit for bot execution. */
  private Integer timeLimit;

  /** Wiki */
  private EnumWikipedia wiki;

  /** True if login is done */
  private boolean loginDone;

  /** Actions to be executed */
  private String[] actions;

  /**
   * @param args Command line arguments
   */
  private Bot(String[] args) {

    // Analyze command line arguments
    int currentArg = 0;

    // Process general command line arguments
    boolean done = false;
    timeLimit = null;
    String credentials = null;
    while (!done) {
      if (args.length > currentArg) {
        String arg = args[currentArg];

        if ("-timelimit".equals(arg)) {
          timeLimit = Integer.valueOf(args[currentArg + 1]);
          currentArg += 2;
        } else if ("-credentials".equals(arg)) {
          credentials = args[currentArg + 1];
          currentArg += 2;
        } else {
          done = true;
        }
      }
    }

    // Retrieve wiki
    if (args.length > currentArg) {
      String wikiCode = args[currentArg];
      wiki = EnumWikipedia.getWikipedia(wikiCode);
      if ((wiki == null) || !wikiCode.equals(wiki.getSettings().getCode())) {
        log.warn("Unable to find wiki " + wikiCode);
        return;
      }
    }
    currentArg++;

    // Retrieve user name and password
    String userName = null;
    String password = null;
    if (credentials != null) {
      Properties properties = new Properties();
      try {
        properties.load(new FileReader(credentials));
      } catch (IOException e) {
        log.warn("Unable to load credentials file " + credentials);
        return;
      }
      userName = properties.getProperty("user");
      password = properties.getProperty("password");
    } else {
      if (args.length > currentArg) {
        userName = args[currentArg];
      }
      currentArg++;
      if (args.length > currentArg) {
        password = args[currentArg];
      }
      currentArg++;
    }

    // Retrieve action
    actions = (args.length > currentArg) ?
        Arrays.copyOfRange(args, currentArg, args.length) : null;
    currentArg++;

    // Check arguments
    if ((wiki == null) ||
        (userName == null) ||
        (password == null) ||
        (actions == null) ||
        (actions.length == 0)) {
      log.warn("Some parameters are incorrect");
      return;
    }

    // Login
    loginDone = false;
    LoginWorker loginWorker = new LoginWorker(
        wiki, null, null, EnumLanguage.getDefaultLanguage(),
        userName, password.toCharArray(),
        ConfigurationConstants.VALUE_SAVE_USER_NO_CHANGE, true, false);
    loginWorker.setListener(this);
    loginWorker.start();
  }

  /**
   * Execute an action.
   * 
   * @param args Action and arguments.
   */
  void executeAction(String[] args) {

    // Retrieve action
    int currentArg = 0;
    if (currentArg >= args.length) {
      return;
    }
    String action = args[currentArg];
    currentArg++;

    // Execute action depending on the parameters
    BasicWorker worker = null;
    if ("UpdateDabWarnings".equalsIgnoreCase(action)) {
      Configuration config = Configuration.getConfiguration();
      String start = config.getString(null, ConfigurationValueString.LAST_DAB_WARNING);
      if (args.length > currentArg) {
        if (args[currentArg].equals("*")) {
          start = null;
        } else {
          start = args[currentArg];
        }
      }
      worker = new UpdateDabWarningWorker(wiki, null, start);
    } else if ("UpdateISBNWarnings".equalsIgnoreCase(action)) {
      worker = new UpdateISBNWarningWorker(wiki, null, false);
    } else if ("ListISBNWarnings".equalsIgnoreCase(action)) {
      worker = new UpdateISBNWarningWorker(wiki, null, true);
    } else if ("UpdateISSNWarnings".equalsIgnoreCase(action)) {
      worker = new UpdateISSNWarningWorker(wiki, null, false);
    } else if ("ListISSNWarnings".equalsIgnoreCase(action)) {
      worker = new UpdateISSNWarningWorker(wiki, null, true);
    } else if ("UpdateDuplicateArgsWarnings".equalsIgnoreCase(action)) {
      worker = new UpdateDuplicateArgsWarningWorker(wiki, null, false);
    } else if ("FixCheckWiki".equalsIgnoreCase(action)) {
      List<CheckErrorAlgorithm> algorithms = new ArrayList<CheckErrorAlgorithm>();
      List<CheckErrorAlgorithm> allAlgorithms = new ArrayList<CheckErrorAlgorithm>();
      if (args.length > currentArg) {
        extractAlgorithms(algorithms, allAlgorithms, args, currentArg);
      }
      worker = new AutomaticCWWorker(
          wiki, null, algorithms, 10000, true, allAlgorithms, null, true, false);
    } else if ("MarkCheckWiki".equalsIgnoreCase(action)) {
      List<CheckErrorAlgorithm> algorithms = new ArrayList<CheckErrorAlgorithm>();
      List<CheckErrorAlgorithm> allAlgorithms = new ArrayList<CheckErrorAlgorithm>();
      if (args.length > currentArg) {
        extractAlgorithms(algorithms, allAlgorithms, args, currentArg);
      }
      worker = new AutomaticCWWorker(
          wiki, null, algorithms, 10000, true, allAlgorithms, null, false, false);
    } else if ("ListCheckWiki".equalsIgnoreCase(action)) {
      boolean check = true;
      boolean onlyRecheck = false;
      boolean optionsFinished = false;
      while (!optionsFinished && (args.length > currentArg)) {
        if ("-nocheck".equalsIgnoreCase(args[currentArg])) {
          check = false;
          currentArg++;
        } else if ("-onlyRecheck".equalsIgnoreCase(args[currentArg])) {
          onlyRecheck = true;
          currentArg++;
        } else {
          optionsFinished = true;
        }
      }
      if (args.length > currentArg + 2) {
        File dumpFile = getDumpFile(args[currentArg]);
        List<CheckErrorAlgorithm> algorithms = new ArrayList<CheckErrorAlgorithm>();
        extractAlgorithms(algorithms, null, args, currentArg + 2);
        if (args[currentArg + 1].startsWith("wiki:")) {
          String pageName = args[currentArg + 1].substring(5);
          worker = new ListCWWorker(
              wiki, null, dumpFile, pageName,
              algorithms, check, onlyRecheck);
        } else {
          File output = new File(args[currentArg + 1]);
          worker = new ListCWWorker(
              wiki, null, dumpFile, output,
              algorithms, check);
        }
      }
    }
    if (worker != null) {
      worker.setListener(this);
      worker.setTimeLimit(timeLimit);
      worker.start();
    }
  }

  /**
   * @param algorithms List of selected algorithms.
   * @param allAlgorithms List of all possible algorithms.
   * @param args Arguments.
   * @param startIndex Start index in the arguments.
   */
  private void extractAlgorithms(
      List<CheckErrorAlgorithm> algorithms,
      List<CheckErrorAlgorithm> allAlgorithms,
      String[] args, int startIndex) {
    for (int i = startIndex; i < args.length; i++) {
      boolean addition = false;
      String algorithmNumber = args[i];
      if (algorithmNumber.startsWith("+")) {
        addition = true;
        algorithmNumber = algorithmNumber.substring(1);
      }
      if (algorithmNumber.equals("*") || algorithmNumber.equals("!")) {
        // NOTE: Allowing "!" because of Eclipse bug with "*"
        // https://bugs.eclipse.org/bugs/show_bug.cgi?id=212264
        List<CheckErrorAlgorithm> possibleAlgorithms = CheckErrorAlgorithms.getAlgorithms(wiki);
        for (CheckErrorAlgorithm algorithm : possibleAlgorithms) {
          if ((algorithm != null) && algorithm.isAvailable()) {
            if (!addition) {
              algorithms.add(algorithm);
            }
            if (allAlgorithms != null) {
              allAlgorithms.add(algorithm);
            }
          }
        }
      } else {
        CheckErrorAlgorithm algorithm = CheckErrorAlgorithms.getAlgorithm(wiki, Integer.parseInt(algorithmNumber));
        if (algorithm != null) {
          if (!addition) {
            algorithms.add(algorithm);
          }
          if (allAlgorithms != null) {
            allAlgorithms.add(algorithm);
          }
        }
      }
    }
  }

  /**
   * @param path Path to the dump file.
   * @return Dump file.
   */
  private File getDumpFile(String path) {
    File file = new File(path);
    if (file.exists() && file.isFile() && file.canRead()) {
      return file;
    }
    File parent = file.getParentFile();
    if ((parent == null) || (!parent.exists()) || (!parent.isDirectory())) {
      return null;
    }
    final String filename = file.getName();
    final int starIndex = filename.indexOf('$');
    if (starIndex < 0) {
      return null;
    }
    String[] filenames = parent.list(new FilenameFilter() {
      
      @Override
      public boolean accept(@SuppressWarnings("unused") File dir, String name) {
        if (name.startsWith(filename.substring(0, starIndex)) &&
            name.endsWith(filename.substring(starIndex + 1))) {
          return true;
        }
        return false;
      }
    });
    if (filenames.length == 0) {
      return null;
    }
    Arrays.sort(filenames);
    return new File(parent, filenames[filenames.length - 1]);
  }

  /**
   * @param worker
   * @see org.wikipediacleaner.gui.swing.basic.BasicWorkerListener#beforeStart(org.wikipediacleaner.gui.swing.basic.BasicWorker)
   */
  @Override
  public void beforeStart(BasicWorker worker) {
    // Do nothing
  }

  /**
   * @param worker
   * @see org.wikipediacleaner.gui.swing.basic.BasicWorkerListener#afterStart(org.wikipediacleaner.gui.swing.basic.BasicWorker)
   */
  @Override
  public void afterStart(BasicWorker worker) {
    // Do nothing
  }

  /**
   * @param worker
   * @see org.wikipediacleaner.gui.swing.basic.BasicWorkerListener#beforeFinished(org.wikipediacleaner.gui.swing.basic.BasicWorker)
   */
  @Override
  public void beforeFinished(BasicWorker worker) {
    // Do nothing
  }

  /**
   * @param worker
   * @param ok
   * @see org.wikipediacleaner.gui.swing.basic.BasicWorkerListener#afterFinished(org.wikipediacleaner.gui.swing.basic.BasicWorker, boolean)
   */
  @Override
  public void afterFinished(BasicWorker worker, boolean ok) {
    if (!ok) {
      System.exit(1);
    }
    if (loginDone) {
      System.exit(0);
    }
    loginDone = true;
    executeAction(actions);
  }
}
