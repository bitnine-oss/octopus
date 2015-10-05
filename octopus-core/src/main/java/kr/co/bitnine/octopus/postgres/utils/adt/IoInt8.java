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

package kr.co.bitnine.octopus.postgres.utils.adt;

import kr.co.bitnine.octopus.postgres.libpq.ByteBuffers;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public final class IoInt8 implements IoFunction {
    @Override
    public Object in(byte[] bytes) {
        return Long.valueOf(new String(bytes, StandardCharsets.UTF_8));
    }

    @Override
    public byte[] out(Object value) {
        return value == null ? null : String.valueOf(value).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public Object recv(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getLong();
    }

    @Override
    public byte[] send(Object value) {
        return value == null ? null : ByteBuffer.allocate(ByteBuffers.LONG_BYTES).putLong((Long) value).array();
    }
}
