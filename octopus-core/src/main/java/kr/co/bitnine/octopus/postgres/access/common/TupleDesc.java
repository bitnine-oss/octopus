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

package kr.co.bitnine.octopus.postgres.access.common;

import kr.co.bitnine.octopus.postgres.catalog.PostgresAttribute;
import kr.co.bitnine.octopus.postgres.utils.adt.FormatCode;

import java.util.Arrays;

public final class TupleDesc {
    private final PostgresAttribute[] attrs;
    private final FormatCode[] resultFormats;

    public TupleDesc(PostgresAttribute[] attrs, FormatCode[] resultFormats) {
        this.attrs = attrs;

        if (resultFormats.length > 1) {
            if (resultFormats.length != attrs.length) {
                // TODO: throw PROTOCOL_VIOLATION
            }
            this.resultFormats = resultFormats;
        } else if (resultFormats.length > 0) {
            FormatCode[] formats = new FormatCode[attrs.length];
            Arrays.fill(formats, resultFormats[0]);
            this.resultFormats = formats;
        } else {
            FormatCode[] formats = new FormatCode[attrs.length];
            Arrays.fill(formats, FormatCode.TEXT);
            this.resultFormats = formats;
        }
    }

    public PostgresAttribute[] getAttributes() {
        return Arrays.copyOf(attrs, attrs.length);
    }

    public int getNumAttributes() {
        return attrs.length;
    }

    public FormatCode[] getResultFormats() {
        return Arrays.copyOf(resultFormats, resultFormats.length);
    }
}
