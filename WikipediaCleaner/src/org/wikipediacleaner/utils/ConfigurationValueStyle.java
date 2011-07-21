/*
 *  WikipediaCleaner: A tool to help on Wikipedia maintenance tasks.
 *  Copyright (C) 2011  Nicolas Vervelle
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.wikipediacleaner.utils;

import java.awt.Color;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


/**
 * Configuration for Style attributes.
 */
public enum ConfigurationValueStyle {

  COMMENTS("Comments", new StyleProperties(
      true,
      true, Color.GRAY,
      false, Color.WHITE,
      true, false, false, false));

  private final static String PROPERTY_ENABLED = "Enabled";
  private final static String PROPERTY_FOREGROUND = "Foreground";
  private final static String PROPERTY_FOREGROUND_COLOR = "ForegroundColor";
  private final static String PROPERTY_BACKGROUND = "Background";
  private final static String PROPERTY_BACKGROUND_COLOR = "BackgroundColor";
  private final static String PROPERTY_ITALIC = "Italic";
  private final static String PROPERTY_BOLD = "Bold";
  private final static String PROPERTY_UNDERLINE = "Underline";
  private final static String PROPERTY_STRIKE = "Strike";

  /**
   * Attribute name.
   */
  private final String name;

  /**
   * Attribute default value.
   */
  private final StyleProperties defaultValue;

  /**
   * @param name Attribute name.
   * @param defaultValue Attribute default value.
   */
  ConfigurationValueStyle(String name, StyleProperties defaultValue) {
    this.name = name;
    this.defaultValue = defaultValue;
  }

  /**
   * @param preferences Root of preferences for WPCleaner.
   * @param attribute Attribute.
   * @return Current value of the attribute.
   */
  static StyleProperties getValue(Preferences preferences, ConfigurationValueStyle attribute) {
    if (attribute == null) {
      return null;
    }
    return attribute.getValue(preferences);
  }

  /**
   * @param preferences Root of preferences for WPCleaner.
   * @return Current value of the attribute.
   */
  StyleProperties getValue(Preferences preferences) {
    StyleProperties defaultProperties = getDefaultValue();
    preferences = getStyleNode(preferences, false);
    if (preferences == null) {
      return defaultProperties;
    }
    boolean enabled = preferences.getBoolean(
        PROPERTY_ENABLED,
        defaultProperties.getEnabled());
    boolean foreground = preferences.getBoolean(
        PROPERTY_FOREGROUND,
        defaultProperties.getForeground());
    Color foregroundColor = new Color(preferences.getInt(
        PROPERTY_FOREGROUND_COLOR,
        defaultProperties.getForegroundColor().getRGB()));
    boolean background = preferences.getBoolean(
        PROPERTY_BACKGROUND,
        defaultProperties.getBackground());
    Color backgroundColor = new Color(preferences.getInt(
        PROPERTY_BACKGROUND_COLOR,
        defaultProperties.getBackgroundColor().getRGB()));
    boolean italic = preferences.getBoolean(
        PROPERTY_ITALIC,
        defaultProperties.getItalic());
    boolean bold = preferences.getBoolean(
        PROPERTY_BOLD,
        defaultProperties.getBold());
    boolean underline = preferences.getBoolean(
        PROPERTY_UNDERLINE,
        defaultProperties.getUnderline());
    boolean strikeThrough = preferences.getBoolean(
        PROPERTY_STRIKE,
        defaultProperties.getStrikeThrough());
    return new StyleProperties(
        enabled,
        foreground, foregroundColor,
        background, backgroundColor,
        italic, bold, underline, strikeThrough);
  }

  /**
   * @param preferences Root of preferences for WPCleaner.
   * @param attribute Attribute.
   * @param value New value of the attribute.
   */
  static void setValue(Preferences preferences, ConfigurationValueStyle attribute, StyleProperties value) {
    if (attribute == null) {
      return;
    }
    attribute.setValue(preferences, value);
  }

  /**
   * @param preferences Root of preferences for WPCleaner.
   * @param value New value of the attribute.
   */
  void setValue(Preferences preferences, StyleProperties value) {
    preferences = getStyleNode(preferences, true);
    if (preferences == null) {
      return;
    }
    preferences.putBoolean(PROPERTY_ENABLED, value.getEnabled());
    preferences.putBoolean(PROPERTY_FOREGROUND, value.getForeground());
    preferences.putInt(PROPERTY_FOREGROUND_COLOR, value.getForegroundColor().getRGB());
    preferences.putBoolean(PROPERTY_BACKGROUND, value.getBackground());
    preferences.putInt(PROPERTY_BACKGROUND_COLOR, value.getBackgroundColor().getRGB());
    preferences.putBoolean(PROPERTY_ITALIC, value.getItalic());
    preferences.putBoolean(PROPERTY_BOLD, value.getBold());
    preferences.putBoolean(PROPERTY_UNDERLINE, value.getUnderline());
    preferences.putBoolean(PROPERTY_STRIKE, value.getStrikeThrough());
  }

  /**
   * @param preferences Root of preferences for WPCleaner.
   * @param create Flag indicating if the node should be created.
   * @return Node for the style.
   */
  private Preferences getStyleNode(Preferences preferences, boolean create) {
    try {
      if (preferences == null) {
        return null;
      }
      if (!create && !preferences.nodeExists("Styles")) {
        return null;
      }
      preferences = preferences.node("Styles");
      if (!create && !preferences.nodeExists(getName())) {
        return null;
      }
      preferences = preferences.node(getName());
      return preferences;
    } catch (BackingStoreException e) {
      return null;
    }
  }

  /**
   * @return Name of the configuration attribute.
   */
  public String getName() {
    return name;
  }

  /**
   * @return Default value of the attribute.
   */
  public StyleProperties getDefaultValue() {
    return defaultValue;
  }

  /**
   * Holder for Style properties.
   */
  public static class StyleProperties {
    private final boolean enabled;

    private final boolean foreground;
    private final Color foregroundColor;
    private final boolean background;
    private final Color backgroundColor;

    private final boolean italic;
    private final boolean bold;
    private final boolean underline;
    private final boolean strikeThrough;

    /**
     * @param enabled Is style enabled ?
     * @param foreground Is foreground color activated ?
     * @param foregroundColor Foreground color.
     * @param background Is background color activated ?
     * @param backgroundColor Background color.
     * @param italic Italic ?
     * @param bold Bold ?
     * @param underline Underline ?
     * @param strikeThrough Strike through ?
     */
    public StyleProperties(
        boolean enabled,
        boolean foreground, Color foregroundColor,
        boolean background, Color backgroundColor,
        boolean italic, boolean bold,
        boolean underline, boolean strikeThrough) {
      this.enabled = enabled;
      this.foreground = foreground;
      this.foregroundColor = foregroundColor;
      this.background = background;
      this.backgroundColor = backgroundColor;
      this.italic = italic;
      this.bold = bold;
      this.underline = underline;
      this.strikeThrough = strikeThrough;
    }

    /**
     * @return Is style enabled ?
     */
    public boolean getEnabled() {
      return enabled;
    }

    /**
     * @return Is foreground color activated ?
     */
    public boolean getForeground() {
      return foreground;
    }

    /**
     * @return Foreground color.
     */
    public Color getForegroundColor() {
      return foregroundColor;
    }

    /**
     * @return Is background color activated ?
     */
    public boolean getBackground() {
      return background;
    }

    /**
     * @return Background color.
     */
    public Color getBackgroundColor() {
      return backgroundColor;
    }

    /**
     * @return Is italic ?
     */
    public boolean getItalic() {
      return italic;
    }

    /**
     * @return Is bold ?
     */
    public boolean getBold() {
      return bold;
    }

    /**
     * @return Is underline ?
     */
    public boolean getUnderline() {
      return underline;
    }

    /**
     * @return Is strike through ?
     */
    public boolean getStrikeThrough() {
      return strikeThrough;
    }
  }
}