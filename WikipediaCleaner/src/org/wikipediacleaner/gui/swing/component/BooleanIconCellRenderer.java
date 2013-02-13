/*
 *  WikipediaCleaner: A tool to help on Wikipedia maintenance tasks.
 *  Copyright (C) 2013  Nicolas Vervelle
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

package org.wikipediacleaner.gui.swing.component;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.wikipediacleaner.gui.swing.basic.Utilities;
import org.wikipediacleaner.images.EnumImageSize;


/**
 * A cell renderer for Boolean with icons.
 */
public class BooleanIconCellRenderer extends DefaultTableCellRenderer {

  private static final long serialVersionUID = 1557809942272845154L;

  /**
   * Icon to use when the value is true.
   */
  private final ImageIcon iconTrue;

  /**
   * Icon to use when the value is false.
   */
  private final ImageIcon iconFalse;

  /**
   * Constructor.
   */
  public BooleanIconCellRenderer(String trueName, String falseName) {
    super();
    iconTrue = (trueName != null) ?
        Utilities.getImageIcon(trueName, EnumImageSize.SMALL) :
        null;
    iconFalse = (falseName != null) ?
        Utilities.getImageIcon(falseName, EnumImageSize.SMALL) :
        null;
  }

  /**
   * Returns the table cell renderer for Boolean.
   * 
   * @param table  the <code>JTable</code>
   * @param value  the value to assign to the cell at
   *      <code>[row, column]</code>
   * @param isSelected true if cell is selected
   * @param hasFocus true if cell has focus
   * @param row  the row of the cell to render
   * @param column the column of the cell to render
   * @return the default table cell renderer
   * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
   */
  @Override
  public Component getTableCellRendererComponent(
      JTable table, Object value,
      boolean isSelected, boolean hasFocus,
      int row, int column) {
    if (value instanceof Boolean) {
      Boolean booleanValue = (Boolean) value;
      ImageIcon icon = booleanValue ? iconTrue : iconFalse;
      if (icon != null) {
        return new JLabel(icon);
      }
      return super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
    }
    Component component = super.getTableCellRendererComponent(
        table, value, isSelected, hasFocus, row, column);
    return component;
  }
}