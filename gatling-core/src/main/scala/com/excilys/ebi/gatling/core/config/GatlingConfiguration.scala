/**
 * Copyright 2011-2012 eBusiness Information, Groupe Excilys (www.excilys.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.excilys.ebi.gatling.core.config

import scala.io.Codec
import scala.tools.nsc.io.Path.string2path
import scala.tools.nsc.io.Path
import com.excilys.ebi.gatling.core.config.GatlingConfiguration.GATLING_DEFAULT_CONFIG_FILE
import com.excilys.ebi.gatling.core.result.reader.DataReader
import com.excilys.ebi.gatling.core.result.writer.DataWriter
import com.excilys.ebi.gatling.core.util.DateHelper.parseReadableDate
import com.excilys.ebi.gatling.core.util.StringHelper.EMPTY
import com.excilys.ebi.gatling.core.init.Initializable
import grizzled.slf4j.Logging

/**
 * Configuration loader of Gatling
 */
object GatlingConfiguration extends Initializable {

	val GATLING_DEFAULT_CONFIG_FILE = "gatling.conf"

	@volatile private var instance: GatlingConfiguration = _

	def setUp(configFileName: Option[String], dataFolder: Option[String], requestBodiesFolder: Option[String], resultsFolder: Option[String], simulationsFolder: Option[String]) {
		if (initialized.compareAndSet(false, true)) {
			instance = new GatlingConfiguration(configFileName, dataFolder, requestBodiesFolder, resultsFolder, simulationsFolder)
		} else {
			throw new UnsupportedOperationException("GatlingConfig already set up")
		}
	}

	def configuration = if (initialized.get) instance else throw new UnsupportedOperationException("Can't access configuration instance if it hasn't been set up")
}

class GatlingConfiguration(
		configFileName: Option[String] = None,
		dataFolder: Option[String] = None,
		requestBodiesFolder: Option[String] = None,
		resultsFolder: Option[String] = None,
		simulationsFolder: Option[String] = None) extends Logging {

	/**
	 * Contains the configuration of Gatling
	 */
	val fileConfiguration: GatlingFileConfiguration =
		try {
			// Locate configuration file, depending on users options
			val configFile = configFileName map { fileName =>
				info("Loading custom configuration file: conf/" + fileName)
				fileName
			} getOrElse {
				info("Loading default configuration file")
				GATLING_DEFAULT_CONFIG_FILE
			}

			GatlingFileConfiguration.fromFile(configFile)
		} catch {
			case e =>
				error(e)
				throw new Exception("Could not parse configuration file!")
		}

	val resultsFolderPath : Option[Path] = resultsFolder.map(s => s)
	val dataFolderPath: Option[Path] = dataFolder.map(s => s)
	val requestBodiesFolderPath: Option[Path] = requestBodiesFolder.map(s => s)
	val simulationsFolderPath: Option[Path] = simulationsFolder.map(s => s)

	/**
	 * Gatling global encoding value
	 */
	val encoding = fileConfiguration("gatling.encoding", Codec.UTF8.name)

	/**
	 * Gatling simulation timeout value
	 */
	val simulationTimeOut = fileConfiguration("gatling.simulation.timeout", 86400)

	val simulationScalaPackage = fileConfiguration("gatling.simulation.scalaPackage", EMPTY)

	val chartingIndicatorsLowerBound = fileConfiguration("gatling.charting.indicators.lowerBound", 100)

	val chartingIndicatorsHigherBound = fileConfiguration("gatling.charting.indicators.higherBound", 500)

	val chartingMaxPlotPerSerie = fileConfiguration("gatling.charting.maxPlotPerSerie", 5000)

	val chartingTimeWindowLowerBound = fileConfiguration("gatling.charting.timeWindow.lowerBound", EMPTY) match {
		case EMPTY => Long.MinValue
		case string => parseReadableDate(string).getMillis
	}

	val chartingTimeWindowHigherBound = fileConfiguration("gatling.charting.timeWindow.higherBound", EMPTY) match {
		case EMPTY => Long.MaxValue
		case string => parseReadableDate(string).getMillis
	}

	val dataWriterClass = Class.forName(fileConfiguration("gatling.data.writer", "com.excilys.ebi.gatling.core.result.writer.FileDataWriter")).asInstanceOf[Class[DataWriter]]

	val dataReaderClass = Class.forName(fileConfiguration("gatling.data.reader", "com.excilys.ebi.gatling.charts.result.reader.FileDataReader")).asInstanceOf[Class[DataReader]]
}
