/**
 * Copyright [2015] [Christian Loehnert]
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
package de.ks.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.spec.KeySpec;
import java.util.Base64;

public class Smoke {
  private static String getDesc() {
    return String.valueOf((char) process(first(), 1)) //
      + String.valueOf((char) process(second(), 2)) //
      + String.valueOf((char) process(third(), 4))//
      + String.valueOf((char) process(fourth(), 3));
  }

  private static int process(int first, int second) {
    return first >> second;
  }

  private static int fourth() {
    return 256;
  }

  private static int third() {
    return 1328;
  }

  private static int second() {
    return 276;
  }

  private static int first() {
    return 130;
  }

  public static final Smoke instance = new Smoke();
  private final Cipher smokey;
  private final Cipher fadey;
  private final Key origin;
  private final Base64.Encoder multiplier;
  private final Base64.Decoder stringifier;

  private Smoke() {
    try {
      SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
      KeySpec spec = new PBEKeySpec(getClass().getName().toCharArray(), getClass().getPackage().getName().getBytes("UTF8"), 65536, 128);
      SecretKey tmp = factory.generateSecret(spec);
      origin = new SecretKeySpec(tmp.getEncoded(), getDesc().trim());

      smokey = Cipher.getInstance(getDesc().trim());
      smokey.init(Cipher.ENCRYPT_MODE, origin);
      fadey = Cipher.getInstance(getDesc().trim());
      fadey.init(Cipher.DECRYPT_MODE, origin);

      stringifier = Base64.getDecoder();
      multiplier = Base64.getEncoder();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public String emerge(String input) {
    try {
      byte[] utf8 = input.getBytes("UTF8");
      byte[] enc = smokey.doFinal(utf8);
      enc = multiplier.encode(enc);
      return new String(enc);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public String fadeAway(String input) {
    try {
      byte[] dec = stringifier.decode(input.getBytes());
      byte[] utf8 = fadey.doFinal(dec);
      return new String(utf8, "UTF8");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
