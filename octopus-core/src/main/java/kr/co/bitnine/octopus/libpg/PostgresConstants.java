/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kr.co.bitnine.octopus.libpg;

public abstract class PostgresConstants
{
    /* Byte.BYTES, Integer.BYTES since Java 8 */
    static final int BYTE_BYTES = Byte.SIZE / Byte.SIZE;
    static final int SHORT_BYTES = Short.SIZE / Byte.SIZE;
    static final int INTEGER_BYTES = Integer.SIZE / Byte.SIZE;

    public static final int CANCEL_REQUEST_CODE = (1234 << 16) | 5678;
    public static final int SSL_REQUEST_CODE = (1234 << 16) | 5679;

    public static int PROTOCOL_VERSION(int major, int minor)
    {
        return (major << 16) | minor;
    }
}
