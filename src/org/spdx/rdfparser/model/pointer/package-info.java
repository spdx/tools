/**
 * Copyright (c) 2016 Source Auditor Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
/**
 * RDF model classes which implement the proposed W3C Pointer classes.
 * From the W3C documentation, pointers are entities that permit identifying 
 * a portion or segment of a piece of content - making use of the 
 * Resource Description Framework (RDF). It also describes a number of specific
 *  types of pointers that permit portions of a document to be referred to in 
 *  different ways. When referring to a specific part of, say, a piece of web content, 
 *  it is useful to be able to have a consistent manner by which to refer to a particular 
 *  segment of a web document, to have a variety of ways by which to refer to that 
 *  same segment, and to make the reference robust in the face of changes to that document.
 *  
 *  Pointers are used to describe SPDX Snippet ranges.
 *  
 * @author Gary O'Neall
 *
 */
package org.spdx.rdfparser.model.pointer;