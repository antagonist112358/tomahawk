package net.mentalarray.doozie.tests

import org.specs2.mutable.Specification

class PigTaskRunnerSpec extends Specification {

  sequential

  private def getInstance : PigTaskRunner = {
    new PigTaskRunner()
  }

  private def getTaskInstance : PigTask = {
    new PigTask("testTask")
  }

  "PigTaskRunnerSpec" should {

    "throw an exception when trying to run an invalid task" in {
      getInstance.run(getTaskInstance) must throwA[WorkflowStateException]
    }

    "allow test-level execution where the command is not actually run" in {
      val task = getTaskInstance
      task.setScript( """
                        RAWLOAD20 = LOAD '/data/raw/yield/wafer/20nm/Pull20' USING PigStorage (',') as
                        (ROOT_LOT_ID:chararray, WAFER_ID:int, TKIN_TIME:chararray, TKOUT_TIME:chararray,
                        EDS_LOT_TYPE:chararray, EDS_TESTER:chararray, PART_ID:chararray, PGM_ID:chararray,
                        ITEM:chararray, VALUE:float, TEST_TYPE:chararray, RETEST_CNT:chararray,
                        PROCESS_ID:chararray, PROBE_CARD_ID: chararray, EDS_LOT_ID:chararray);

                        OUTPUT1 = FOREACH RAWLOAD20 GENERATE ROOT_LOT_ID,WAFER_ID;

                        STORE OUTPUT1 into '/tmp/pigDump'
                        using PigStorage(',');

                      """)
      getInstance.test(task) must not(throwA[Exception])
    }

    "execute pig script" in {
      val task = getTaskInstance
      task.setScript( """
                     RAWLOAD20 = LOAD '/data/raw/yield/wafer/20nm/Pull20' USING PigStorage (',') as
                     (ROOT_LOT_ID:chararray, WAFER_ID:int, TKIN_TIME:chararray, TKOUT_TIME:chararray,
                     EDS_LOT_TYPE:chararray, EDS_TESTER:chararray, PART_ID:chararray, PGM_ID:chararray,
                     ITEM:chararray, VALUE:float, TEST_TYPE:chararray, RETEST_CNT:chararray,
                     PROCESS_ID:chararray, PROBE_CARD_ID: chararray, EDS_LOT_ID:chararray);

                     OUTPUT1 = FOREACH RAWLOAD20 GENERATE ROOT_LOT_ID,WAFER_ID;

                     STORE OUTPUT1 into '/tmp/pigDump'
                     using PigStorage(',');

                      """)
      getInstance.run(task) must not(throwA[Exception])
    }

    "execute pig script with exec correctly" in {
      val task = getTaskInstance
      task.setScript( """
                     RAWLOAD20 = LOAD '/data/raw/yield/wafer/20nm/Pull20' USING PigStorage (',') as
                     (ROOT_LOT_ID:chararray, WAFER_ID:int, TKIN_TIME:chararray, TKOUT_TIME:chararray,
                     EDS_LOT_TYPE:chararray, EDS_TESTER:chararray, PART_ID:chararray, PGM_ID:chararray,
                     ITEM:chararray, VALUE:float, TEST_TYPE:chararray, RETEST_CNT:chararray,
                     PROCESS_ID:chararray, PROBE_CARD_ID: chararray, EDS_LOT_ID:chararray);

                     OUTPUT1 = FOREACH RAWLOAD20 GENERATE ROOT_LOT_ID,WAFER_ID;

                     STORE OUTPUT1 into '/tmp/pigDump1'
                     using PigStorage(',');

                     exec;

                     RAWLOAD30 = FOREACH RAWLOAD20 {
                        GENERATE
                        ROOT_LOT_ID,
                        WAFER_ID,
                        PROCESS_ID;
                     };

                     STORE RAWLOAD30 into '/tmp/pigDump2' using PigStorage(',');
                      """)
      getInstance.run(task) must not(throwA[Exception])
    }

  }
}