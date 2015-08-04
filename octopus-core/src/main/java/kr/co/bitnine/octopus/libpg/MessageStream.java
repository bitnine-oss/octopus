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

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class MessageStream
{
    private static final int SEND_BUFFER_SIZE_DEFAULT = 8 * 1024;
    private static final int RECV_BUFFER_SIZE_DEFAULT = 8 * 1024;

    private final SocketChannel socketChannel;
    private final ByteBuffer sendBuffer = ByteBuffer.allocate(SEND_BUFFER_SIZE_DEFAULT);
    private final ByteBuffer recvBuffer = ByteBuffer.allocate(RECV_BUFFER_SIZE_DEFAULT);

    public MessageStream(SocketChannel socketChannel)
    {
        this.socketChannel = socketChannel;

        // make receive buffer has no remaining elements
        recvBuffer.flip();
    }

    public Message getInitialMessage() throws Exception
    {
        byte[] body = getMessageBody();
        return new Message(body);
    }

    public Message getMessage() throws Exception
    {
        char type = getChar();
        byte[] body = getMessageBody();
        return new Message(type, body);
    }

    public void putMessage(Message message) throws IOException
    {
        char type = message.getType();
        putChar(type);
        byte[] body = message.getBody();
        putInt(PostgresConstants.INTEGER_BYTES + body.length);
        putBytes(body);
    }

    // Flush sendBuffer so client will see buffered messages immediately
    public void putMessageAndFlush(Message message) throws IOException
    {
        putMessage(message);
        flush();
    }

    private byte[] getMessageBody() throws Exception
    {
        int len = getInt();
        /* length count includes itself */
        if (len < PostgresConstants.INTEGER_BYTES) {
            PostgresException pge = new PostgresException(
                    PostgresException.Severity.FATAL,
                    PostgresException.SQLSTATE.PROTOCOL_VIOLATION,
                    "incomplete packet");
            putMessageAndFlush(pge.toMessage());
            throw pge;
        }
        byte[] body = new byte[len - PostgresConstants.INTEGER_BYTES];
        getBytes(body);
        return body;
    }

    private char getChar() throws IOException
    {
        if (recvBuffer.remaining() < PostgresConstants.BYTE_BYTES)
            read();

        return (char) recvBuffer.get();
    }

    private int getInt() throws IOException
    {
        if (recvBuffer.remaining() < PostgresConstants.INTEGER_BYTES)
            read();

        return recvBuffer.getInt();
    }

    private void getBytes(byte[] bytes) throws IOException
    {
        int pos = 0;
        int len = bytes.length;
        while (len > 0) {
            if (!recvBuffer.hasRemaining())
                read();

            int amount = recvBuffer.remaining();
            if (amount > len)
                amount = len;
            recvBuffer.get(bytes, pos, amount);

            pos += amount;
            len -= amount;
        }
    }

    private void putChar(char c) throws IOException
    {
        if (sendBuffer.remaining() < PostgresConstants.BYTE_BYTES)
            flush();

        sendBuffer.put((byte) c);
    }

    private void putInt(int i) throws IOException
    {
        if (sendBuffer.remaining() < PostgresConstants.INTEGER_BYTES)
            flush();

        sendBuffer.putInt(i);
    }

    private void putBytes(byte[] bytes) throws IOException
    {
        int pos = 0;
        int len = bytes.length;
        while (len > 0) {
            if (!sendBuffer.hasRemaining())
                flush();

            int amount = sendBuffer.remaining();
            if (amount > len)
                amount = len;
            sendBuffer.put(bytes, pos, amount);

            pos += amount;
            len -= amount;
        }
    }

    private void read() throws IOException
    {
        recvBuffer.compact();

        /*
         * It is guaranteed that if a channel is in blocking mode and
         * there is at least one byte remaining in the buffer then
         * read() will block until at least one byte is read.
         */
        int ret = socketChannel.read(recvBuffer);
        if (ret == -1)
            throw new EOFException();

        recvBuffer.flip();
    }

    public void flush() throws IOException
    {
        sendBuffer.flip();

        while (sendBuffer.hasRemaining())
            socketChannel.write(sendBuffer);

        sendBuffer.clear();
    }
}
