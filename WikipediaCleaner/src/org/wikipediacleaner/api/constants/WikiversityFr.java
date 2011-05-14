/*
 *  WikipediaCleaner: A tool to help on Wikipedia maintenance tasks.
 *  Copyright (C) 2008  Nicolas Vervelle
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

package org.wikipediacleaner.api.constants;

import java.awt.ComponentOrientation;


/**
 * Configuration for <a href="http://fr.wikiversity.org/">Wikiversity FR</a>.
 */
class WikiversityFr {

  private final static String baseUrl = "http://fr.wikiversity.org/w/";

  final static String code     = "v:fr";
  final static String codeCW   = "frwikiversity";
  final static String name     = "Wikiversité";

  final static String apiUrl   = baseUrl + "api.php";
  final static String queryUrl = baseUrl + "query.php";
  final static String indexUrl = baseUrl + "index.php";

  final static ComponentOrientation orientation = ComponentOrientation.LEFT_TO_RIGHT;

  final static String configuration = "Utilisateur:NicoV/WikiCleanerConfiguration";
}