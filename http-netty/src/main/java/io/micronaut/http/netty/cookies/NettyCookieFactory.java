/*
 * Copyright 2017-2018 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.http.netty.cookies;

import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.cookie.CookieFactory;

/**
 * A {@link CookieFactory} for constructing {@link Cookie} instances for Netty
 *
 * @author Graeme Rocher
 * @since 1.0
 */
public class NettyCookieFactory implements CookieFactory {

    @Override
    public Cookie create(String name, String value) {
        return new NettyCookie(name, value);
    }
}
