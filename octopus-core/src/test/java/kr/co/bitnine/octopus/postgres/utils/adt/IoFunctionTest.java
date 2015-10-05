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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class IoFunctionTest {
    @Test
    public void testInt() throws Exception {
        IoInt4 ioInt4 = new IoInt4();

        Integer i = Integer.valueOf(7);

        byte[] bytes = ioInt4.out(i);
        assertEquals(i, ioInt4.in(bytes));

        bytes = ioInt4.send(i);
        assertEquals(i, ioInt4.recv(bytes));
    }

    @Test
    public void testVarchar() throws Exception {
        IoVarchar ioVarchar = new IoVarchar();

        String s = "octopus";

        byte[] bytes = ioVarchar.out(s);
        assertEquals(s, ioVarchar.in(bytes));

        bytes = ioVarchar.send(s);
        assertEquals(s, ioVarchar.recv(bytes));
    }
}
