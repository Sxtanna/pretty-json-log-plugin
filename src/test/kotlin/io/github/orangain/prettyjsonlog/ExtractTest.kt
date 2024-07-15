package io.github.orangain.prettyjsonlog

import io.github.orangain.prettyjsonlog.json.parseJson
import junit.framework.TestCase
import java.time.Instant

private data class ExtractParam(
    val description: String,
    val json: String,
    val expectedTimestamp: Timestamp?,
    val expectedLevel: Level?,
    val expectedMessage: String?,
    val expectedStackTrace: String?
)

private val params = listOf(
    // https://cloud.google.com/logging/docs/structured-logging
    ExtractParam(
        "Cloud Logging",
        """{"severity":"ERROR", "message":"There was an error in the application.", "httpRequest":{"requestMethod":"GET"},"time":"2020-10-12T07:20:50.52Z"}""",
        Timestamp.Parsed(Instant.parse("2020-10-12T07:20:50.52Z")),
        Level.ERROR,
        "There was an error in the application.",
        null,
    ),
    // https://pkg.go.dev/log/slog
    ExtractParam(
        "log/slog in Go",
        """{"time":"2022-11-08T15:28:26.000000000-05:00","level":"INFO","msg":"hello","count":3}""",
        Timestamp.Parsed(Instant.parse("2022-11-08T20:28:26Z")),
        Level.INFO,
        "hello",
        null,
    ),
    // https://github.com/trentm/node-bunyan
    ExtractParam(
        "Bunyan",
        """{"name":"myapp","hostname":"banana.local","pid":40161,"level":30,"msg":"hi","time":"2013-01-04T18:46:23.851Z","v":0}""",
        Timestamp.Parsed(Instant.parse("2013-01-04T18:46:23.851Z")),
        Level.INFO,
        "hi",
        null,
    ),
    // https://github.com/pinojs/pino
    ExtractParam(
        "Pino",
        """{"level":30,"time":1531171074631,"msg":"hello world","pid":657,"hostname":"Davids-MBP-3.fritz.box"}""",
        Timestamp.Parsed(Instant.parse("2018-07-09T21:17:54.631Z")),
        Level.INFO,
        "hello world",
        null,
    ),
    // https://github.com/logfellow/logstash-logback-encoder
    ExtractParam(
        "Logstash Logback Encoder",
        """{"@timestamp":"2019-11-03T10:15:30.123+01:00","@version":"1","message":"My message","logger_name":"org.company.stack.Sample","thread_name":"main","level":"INFO","level_value":20000}""",
        Timestamp.Parsed(Instant.parse("2019-11-03T09:15:30.123Z")),
        Level.INFO,
        "My message",
        null,
    ),
    // https://logging.apache.org/log4j/2.x/manual/json-template-layout.html
    ExtractParam(
        "Log4j2 with EcsLayout.json",
        """{"@timestamp":"2024-07-15T03:36:52.899Z","ecs.version":"1.2.0","error.message":null,"error.stack_trace":"javax.transaction.xa.XAException\n\tat org.apache.activemq.artemis.core.protocol.core.impl.ActiveMQSessionContext.xaCommit(ActiveMQSessionContext.java:495)\n\tat org.apache.activemq.artemis.core.client.impl.ClientSessionImpl.commit(ClientSessionImpl.java:1624)\n\tat com.atomikos.datasource.xa.XAResourceTransaction.commit(XAResourceTransaction.java:557)\n\tat com.atomikos.icatch.imp.CommitMessage.send(CommitMessage.java:52)\n\tat com.atomikos.icatch.imp.CommitMessage.send(CommitMessage.java:23)\n\tat com.atomikos.icatch.imp.PropagationMessage.submit(PropagationMessage.java:67)\n\tat com.atomikos.icatch.imp.Propagator${'$'}PropagatorThread.run(Propagator.java:63)\n\tat com.atomikos.icatch.imp.Propagator.submitPropagationMessage(Propagator.java:42)\n\tat com.atomikos.icatch.imp.HeurHazardStateHandler.onTimeout(HeurHazardStateHandler.java:83)\n\tat com.atomikos.icatch.imp.CoordinatorImp.alarm(CoordinatorImp.java:650)\n\tat com.atomikos.timing.PooledAlarmTimer.notifyListeners(PooledAlarmTimer.java:95)\n\tat com.atomikos.timing.PooledAlarmTimer.run(PooledAlarmTimer.java:82)\n\tat java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1144)\n\tat java.base/java.util.concurrent.ThreadPoolExecutor${'$'}Worker.run(ThreadPoolExecutor.java:642)\n\tat java.base/java.lang.Thread.run(Thread.java:1583)\n","error.type":"javax.transaction.xa.XAException","log.level":"WARN","log.logger":"com.atomikos.datasource.xa.XAResourceTransaction","message": "XA resource 'jms': commit for XID '27726F6F742D6170706C69636174696F6E27313732303738363038353131323031303130:27726F6F742D6170706C69636174696F6E27393136' raised -4: the supplied XID is invalid for this XA resource","process.thread.name":"Atomikos:5"}""",
        Timestamp.Parsed(Instant.parse("2024-07-15T03:36:52.899Z")),
        Level.WARN,
        "XA resource 'jms': commit for XID '27726F6F742D6170706C69636174696F6E27313732303738363038353131323031303130:27726F6F742D6170706C69636174696F6E27393136' raised -4: the supplied XID is invalid for this XA resource",
        "javax.transaction.xa.XAException\n" +
                "\tat org.apache.activemq.artemis.core.protocol.core.impl.ActiveMQSessionContext.xaCommit(ActiveMQSessionContext.java:495)\n" +
                "\tat org.apache.activemq.artemis.core.client.impl.ClientSessionImpl.commit(ClientSessionImpl.java:1624)\n" +
                "\tat com.atomikos.datasource.xa.XAResourceTransaction.commit(XAResourceTransaction.java:557)\n" +
                "\tat com.atomikos.icatch.imp.CommitMessage.send(CommitMessage.java:52)\n" +
                "\tat com.atomikos.icatch.imp.CommitMessage.send(CommitMessage.java:23)\n" +
                "\tat com.atomikos.icatch.imp.PropagationMessage.submit(PropagationMessage.java:67)\n" +
                "\tat com.atomikos.icatch.imp.Propagator\$PropagatorThread.run(Propagator.java:63)\n" +
                "\tat com.atomikos.icatch.imp.Propagator.submitPropagationMessage(Propagator.java:42)\n" +
                "\tat com.atomikos.icatch.imp.HeurHazardStateHandler.onTimeout(HeurHazardStateHandler.java:83)\n" +
                "\tat com.atomikos.icatch.imp.CoordinatorImp.alarm(CoordinatorImp.java:650)\n" +
                "\tat com.atomikos.timing.PooledAlarmTimer.notifyListeners(PooledAlarmTimer.java:95)\n" +
                "\tat com.atomikos.timing.PooledAlarmTimer.run(PooledAlarmTimer.java:82)\n" +
                "\tat java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1144)\n" +
                "\tat java.base/java.util.concurrent.ThreadPoolExecutor\$Worker.run(ThreadPoolExecutor.java:642)\n" +
                "\tat java.base/java.lang.Thread.run(Thread.java:1583)\n",
    ),
)

class ExtractTest : TestCase() {
    fun testExtractTimestamp() {
        params.forEach { param ->
            val (node, _) = parseJson(param.json)!!
            val actual = extractTimestamp(node)
            assertEquals(param.description, param.expectedTimestamp, actual)
        }
    }

    fun testExtractLevel() {
        params.forEach { param ->
            val (node, _) = parseJson(param.json)!!
            val actual = extractLevel(node)
            assertEquals(param.description, param.expectedLevel, actual)
        }
    }

    fun testExtractMessage() {
        params.forEach { param ->
            val (node, _) = parseJson(param.json)!!
            val actual = extractMessage(node)
            assertEquals(param.description, param.expectedMessage, actual)
        }
    }

    fun testStackTrace() {
        params.forEach { param ->
            val (node, _) = parseJson(param.json)!!
            val actual = extractStackTrace(node)
            assertEquals(param.description, param.expectedStackTrace, actual)
        }
    }
}
