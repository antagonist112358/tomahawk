package net.mentalarray.doozie.PigSupport;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pig.PigServer;
import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.impl.PigContext;
import org.apache.pig.impl.util.LogUtils;
import org.apache.pig.tools.grunt.GruntParser;
import org.apache.pig.tools.pigstats.PigStatsUtil;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class PigInBlanket
{
    private final Log log = LogFactory.getLog(getClass());

    BufferedReader in;
    PigServer pig;
    GruntParser parser;

    private static BufferedReader stringToBufferedReader(String input) {
        return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8))));
    }

    public PigInBlanket(String rawScriptText, PigContext pigContext) throws ExecException
    {
        this.in = stringToBufferedReader(rawScriptText);
        this.pig = new PigServer(pigContext);

        if (in != null)
        {
            parser = new GruntParser(in);
            parser.setParams(pig);
        }
    }

    public int[] exec() throws Throwable {
        boolean verbose = "true".equalsIgnoreCase(pig.getPigContext().getProperties().getProperty("verbose"));
        try {
            PigStatsUtil.getEmptyPigStats();
            parser.setInteractive(false);
            return parser.parseStopOnError();
        } catch (Throwable t) {
            LogUtils.writeLog(t, pig.getPigContext().getProperties().getProperty("pig.logfile"),
                    log, verbose, "Pig Stack Trace");
            throw (t);
        }
    }

    public void checkScript() throws Throwable {
        parser.parseOnly();
    }


    public void shutdown() { pig.shutdown(); }
}