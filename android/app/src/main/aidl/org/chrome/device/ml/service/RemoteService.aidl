/* Copyright 2020. All Rights Reserved.
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/
package org.chrome.device.ml.service;

import org.chrome.device.ml.service.RemoteServiceCallback;

interface RemoteService {
  /* Request the process ID of this service, to do evil things with it. */
  int getPid();

  /*
   * Demonstrates some basic types that you can use as parameters
   * and return values in AIDL.
   */
  void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
          double aDouble, String aString);

  /* Register callback */
  void registerCallback(RemoteServiceCallback cb);

  /*
   * Remove a previously registered callback interface.
   */
  void unregisterCallback(RemoteServiceCallback cb);
}
