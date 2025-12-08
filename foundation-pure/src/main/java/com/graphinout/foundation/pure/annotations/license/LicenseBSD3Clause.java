package com.graphinout.foundation.pure.annotations.license;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * License: BSD 3-clause
 *
 * Using this template from <a href="http://opensource.org/licenses/bsd-3-clause">...</a>
 *
 * <pre>
 * Copyright (c) <YEAR>, <OWNER> All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * <Pre>
 *
 * <h2>How to use this annotation</h2> Can annotate classes, methods, packages (package-info.java can contain
 * annotations).
 *
 * Package annotations are inherited on to sub-packages. So annotating package-info in the root of a project annotates
 * the whole project.
 *
 * IMPROVE It would make a somehow cleaner process by using RetentionPolicy SOURCE, then write an AnnotationProcessor to
 * extract some XML file to be put in /META-INF, where another process collects them.
 *
 * @author xamde
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE, ElementType.PACKAGE,
		// TODO parse license annotations also for fields
		ElementType.FIELD })
@LicenseAnnotation(id = "BSD-3-clause", label = "BSD 3-clause License")
public @interface LicenseBSD3Clause {

	/**
	 * @return ISO date string when the dependency and its licenses text have been downloaded
	 */
	String dateRetrieved();

	/**
	 * @return 'year, owner' for license template
	 */
	String yearOwner();

}
