/*
 * Copyright 2014-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.restdocs.payload;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A path that identifies a field in a payload
 * 
 * @author Andy Wilkinson
 *
 */
final class FieldPath {

	private static final Pattern ARRAY_INDEX_PATTERN = Pattern
			.compile("\\[([0-9]+|\\*){0,1}\\]");

	private final String rawPath;

	private final List<String> segments;

	private final boolean precise;

	private FieldPath(String rawPath, List<String> segments, boolean precise) {
		this.rawPath = rawPath;
		this.segments = segments;
		this.precise = precise;
	}

	boolean isPrecise() {
		return this.precise;
	}

	List<String> getSegments() {
		return this.segments;
	}

	@Override
	public String toString() {
		return this.rawPath;
	}

	static FieldPath compile(String path) {
		List<String> segments = extractSegments(path);
		return new FieldPath(path, segments, matchesSingleValue(segments));
	}

	static boolean isArraySegment(String segment) {
		return ARRAY_INDEX_PATTERN.matcher(segment).matches();
	}

	static boolean matchesSingleValue(List<String> segments) {
		for (String segment : segments) {
			if (isArraySegment(segment)) {
				return false;
			}
		}
		return true;
	}

	private static List<String> extractSegments(String path) {
		Matcher matcher = ARRAY_INDEX_PATTERN.matcher(path);
		StringBuilder buffer = new StringBuilder();
		int previous = 0;
		while (matcher.find()) {
			appendWithSeparatorIfNecessary(buffer,
					path.substring(previous, matcher.start(0)));
			appendWithSeparatorIfNecessary(buffer, matcher.group());
			previous = matcher.end(0);
		}
		if (previous < path.length()) {
			appendWithSeparatorIfNecessary(buffer, path.substring(previous));
		}

		String processedPath = buffer.toString();

		return Arrays.asList(processedPath.indexOf('.') > -1 ? processedPath.split("\\.")
				: new String[] { processedPath });
	}

	private static void appendWithSeparatorIfNecessary(StringBuilder buffer,
			String toAppend) {
		if (buffer.length() > 0 && (buffer.lastIndexOf(".") != buffer.length() - 1)
				&& !toAppend.startsWith(".")) {
			buffer.append(".");
		}
		buffer.append(toAppend);
	}
}
