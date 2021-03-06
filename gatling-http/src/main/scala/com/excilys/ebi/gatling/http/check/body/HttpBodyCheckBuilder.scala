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
package com.excilys.ebi.gatling.http.check.body
import com.excilys.ebi.gatling.core.check.ExtractorFactory
import com.excilys.ebi.gatling.core.check.VerifyBuilder
import com.excilys.ebi.gatling.core.session.EvaluatableString
import com.excilys.ebi.gatling.http.check.HttpMultipleCheckBuilder
import com.excilys.ebi.gatling.http.check.HttpCheck
import com.excilys.ebi.gatling.http.request.HttpPhase.CompletePageReceived
import com.ning.http.client.Response

/**
 * This class builds a response body check based on regular expressions
 *
 * @param findExtractorFactory the extractor factory for find
 * @param findAllExtractorFactory the extractor factory for findAll
 * @param countExtractorFactory the extractor factory for count
 * @param expression the function returning the expression representing expression is to be checked
 */
class HttpBodyCheckBuilder(
	findExtractorFactory: Int => ExtractorFactory[Response, String],
	findAllExtractorFactory: ExtractorFactory[Response, Seq[String]],
	countExtractorFactory: ExtractorFactory[Response, Int],
	expression: EvaluatableString)
		extends HttpMultipleCheckBuilder[String](expression, CompletePageReceived) {

	def find: VerifyBuilder[HttpCheck, Response, String] = find(0)

	def find(occurrence: Int) = new VerifyBuilder(httpCheckBuilderFactory, findExtractorFactory(occurrence))

	def findAll = new VerifyBuilder(httpCheckBuilderFactory, findAllExtractorFactory)

	def count = new VerifyBuilder(httpCheckBuilderFactory, countExtractorFactory)
}