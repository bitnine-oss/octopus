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

package kr.co.bitnine.octopus.pgproto;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Message
{
    private char type;
    private byte[] body;
    private ByteBuffer buf;

    public Message(byte[] body)
    {
        this(' ', body);
    }

    public Message(char type, byte[] body)
    {
        this.type = type;
        this.body = body;
        this.buf = ByteBuffer.wrap(body);
    }

    public char getType()
    {
        return type;
    }

    public byte[] getBody()
    {
        return body;
    }

    public static class Builder
    {
        private char type;
        private ByteBuffer buf;

        public Builder(char type)
        {
            this.type = type;
            buf = ByteBuffer.allocate(1024);
        }

        public Builder putChar(char c)
        {
            ByteBufferUtil.enlargeByteBuffer(buf, Constants.BYTE_BYTES);
            buf.put((byte)c);
            return this;
        }

        public Builder putInt(int i)
        {
            ByteBufferUtil.enlargeByteBuffer(buf, Constants.INTEGER_BYTES);
            buf.putInt(i);
            return this;
        }

        public Builder putCString(String s)
        {
            byte[] bytes = s.getBytes(StandardCharsets.US_ASCII);
            ByteBufferUtil.enlargeByteBuffer(buf, bytes.length);
            buf.put(bytes);
            putChar('\0');
            return this;
        }

        public Message build()
        {
            byte[] body = Arrays.copyOf(buf.array(), buf.position());
            return new Message(type, body);
        }
    }

    public int peekInt()
    {
        return buf.getInt(buf.position());
    }

    public short getShort()
    {
        return buf.getShort();
    }

    public int getInt()
    {
        return buf.getInt();
    }

    public String getCString()
    {
        return ByteBufferUtil.getCString(buf);
    }
}
