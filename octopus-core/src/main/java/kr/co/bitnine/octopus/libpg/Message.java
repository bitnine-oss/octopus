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
            ByteBuffers.enlargeByteBuffer(buf, ByteBuffers.BYTE_BYTES);
            buf.put((byte) c);
            return this;
        }

        public Builder putShort(short h)
        {
            ByteBuffers.enlargeByteBuffer(buf, ByteBuffers.SHORT_BYTES);
            buf.putShort(h);
            return this;
        }

        public Builder putInt(int i)
        {
            ByteBuffers.enlargeByteBuffer(buf, ByteBuffers.INTEGER_BYTES);
            buf.putInt(i);
            return this;
        }

        public Builder putBytes(byte[] bytes)
        {
            ByteBuffers.enlargeByteBuffer(buf, bytes.length);
            buf.put(bytes);
            return this;
        }

        public Builder putCString(String s)
        {
            byte[] bytes = s.getBytes(StandardCharsets.US_ASCII);
            ByteBuffers.enlargeByteBuffer(buf, bytes.length);
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

    public static Builder builder(char type)
    {
        return new Builder(type);
    }

    public int peekInt()
    {
        return buf.getInt(buf.position());
    }

    public char getChar()
    {
        return (char) buf.get();
    }

    public short getShort()
    {
        return buf.getShort();
    }

    public int getInt()
    {
        return buf.getInt();
    }

    public byte[] getBytes(int length)
    {
        byte[] bytes = new byte[length];
        buf.get(bytes);
        return bytes;
    }

    public String getCString()
    {
        return ByteBuffers.getCString(buf);
    }
}
