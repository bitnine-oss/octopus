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

import java.io.IOException;
import java.util.Properties;

public interface BackendHandler
{
    void onStartupMessage(Properties params);
    void onCancelRequest(int cancelKey);

    boolean onPasswordMessage(String password);

    int onAuthenticationOk();

    void onQuery(String query);
    void onParse(String name, String query, int[] oids) throws IOException;
}
