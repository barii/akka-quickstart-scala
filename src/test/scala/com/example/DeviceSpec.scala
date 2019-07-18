package com.example

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}

import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class DeviceSpec(_system: ActorSystem)
  extends TestKit(_system)
    with Matchers
    with WordSpecLike
    with BeforeAndAfterAll {

  def this() = this(ActorSystem("DeviceSpec"))


  "reply with empty reading if no temperature is known" in {
    val probe = TestProbe()
    val deviceActor = system.actorOf(Device.props("group", "device"))

    deviceActor.tell(Device.ReadTemperature(requestId = 42), probe.ref)
    val response = probe.expectMsgType[Device.RespondTemperature]
    response.requestId should ===(42L)
    response.value should ===(None)
  }

  "reply with latest temperature reading" in {
    import com.example.Device._

    val probe = TestProbe()
    val deviceActor = system.actorOf(Device.props("group", "device"))

    deviceActor.tell(RecordTemperature(requestId = 1, 24.0), probe.ref)
    probe.expectMsg(TemperatureRecorded(requestId = 1))

    deviceActor.tell(ReadTemperature(requestId = 2), probe.ref)
    val response1 = probe.expectMsgType[RespondTemperature]
    response1.requestId should ===(2L)
    response1.value should ===(Some(24))

    deviceActor.tell(RecordTemperature(requestId = 3, 55.0), probe.ref)
    probe.expectMsg(TemperatureRecorded(requestId = 3))

    deviceActor.tell(ReadTemperature(requestId = 4), probe.ref)
    val response2 = probe.expectMsgType[RespondTemperature]
    response2.requestId should ===(4L)
    response2.value should ===(Some(55.0))
  }
}
