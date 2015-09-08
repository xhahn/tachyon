/*
 * Licensed to the University of California, Berkeley under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package tachyon.security.authentication;

import javax.security.sasl.AuthenticationException;

import com.google.common.base.Strings;

import tachyon.Constants;
import tachyon.conf.TachyonConf;
import tachyon.util.CommonUtils;

/**
 * An authentication provider implementation that allows {@link AuthenticationProvider} to be
 * customized at configuration time. This authentication provider is created if authentication type
 * specified in {@link TachyonConf} is {@link AuthenticationFactory.AuthType#CUSTOM CUSTOM}. It
 * requires the property {@code tachyon.authentication.provider.custom.class} to be set in
 * {@link TachyonConf Configuration} to determine which provider to load.
 */
public class CustomAuthenticationProviderImpl implements AuthenticationProvider {

  private final AuthenticationProvider mCustomProvider;

  public CustomAuthenticationProviderImpl(TachyonConf conf) {
    String customProviderName =
        conf.get(Constants.TACHYON_AUTHENTICATION_PROVIDER_CUSTOM_CLASS, "");
    if (Strings.isNullOrEmpty(customProviderName)) {
      throw new RuntimeException(Constants.TACHYON_AUTHENTICATION_PROVIDER_CUSTOM_CLASS
          + " didn't set");
    }

    Class<?> customProviderClass;
    try {
      customProviderClass = Class.forName(customProviderName);
      if (!AuthenticationProvider.class.isAssignableFrom(customProviderClass)) {
        throw new RuntimeException(customProviderClass + " didn't implement "
            + "interface AuthenticationProvider");
      }
    } catch (ClassNotFoundException cfe) {
      throw new RuntimeException(customProviderName + " not found");
    }

    try {
      mCustomProvider =
          (AuthenticationProvider) CommonUtils.createNewClassInstance(customProviderClass, null,
              null);
    } catch (Exception e) {
      throw new RuntimeException(customProviderClass.getName() + " instantiate failed :"
          + e.getMessage());
    }
  }

  public AuthenticationProvider getCustomProvider() {
    return mCustomProvider;
  }

  @Override
  public void authenticate(String user, String password) throws AuthenticationException {
    mCustomProvider.authenticate(user, password);
  }
}