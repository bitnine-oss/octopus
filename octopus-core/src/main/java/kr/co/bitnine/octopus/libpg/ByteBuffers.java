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

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

abstract class ByteBuffers
{
    static String getCString(ByteBuffer buf)
    {
        int end;
        for (end = buf.position(); end < buf.limit(); end++) {
            if (buf.get(end) == '\0')
                break;
        }
        if (end >= buf.limit())
            throw new BufferUnderflowException();

        String str = new String(buf.array(),
                buf.position(), end - buf.position(),
                StandardCharsets.US_ASCII);

        buf.position(end + 1); // discard '\0'

        return str;
    }

    private static int BYTEBUFFER_CAPACITY_MAX = 1 << 30;

    static ByteBuffer enlargeByteBuffer(ByteBuffer buf, int needed)
    {
        assert !buf.isDirect();

        if (needed < 0)
            throw new IllegalArgumentException("negative enlargement request size: " + needed);

        assert buf.limit() == buf.capacity();
        if (buf.remaining() > needed)
            return buf;

        int minCapacity = buf.position() + needed;
        if (minCapacity > BYTEBUFFER_CAPACITY_MAX)
            throw new OutOfMemoryError("cannot enlarge ByteBuffer containing " + buf.position() + " bytes by " + needed + " more bytes");

        int newCapacity = buf.capacity() * 2;
        while (newCapacity < minCapacity)
            newCapacity *= 2;

        buf.flip();
        return ByteBuffer.allocate(newCapacity).put(buf);
    }
}
