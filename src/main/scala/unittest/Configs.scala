// See LICENSE.SiFive for license details.

package unittest

import Chisel._
import config._
import junctions._
import rocketchip.{BaseConfig, BasePlatformConfig}

class WithJunctionsUnitTests extends Config(
  (pname, site, here) => pname match {
    case HastiId => "HastiTest"
    case HastiKey("HastiTest") => HastiParameters(addrBits = 32, dataBits = 64)
    case NastiKey => NastiParameters(addrBits = 32, dataBits = 64, idBits = 4)
    case rocket.PAddrBits => 32
    case rocket.XLen => 64
    case UnitTests => (p: Parameters) => Seq(
      Module(new junctions.MultiWidthFifoTest),
      Module(new junctions.HastiTest()(p)))
    case _ => throw new CDEMatchError
  })

class JunctionsUnitTestConfig extends Config(new WithJunctionsUnitTests ++ new BasePlatformConfig)

class WithUncoreUnitTests extends Config(
  (pname, site, here) => pname match {
    case uncore.tilelink.TLId => "L1toL2"
    case UnitTests => (q: Parameters) => {
      implicit val p = q
      Seq(
        Module(new uncore.devices.ROMSlaveTest()),
        Module(new uncore.devices.TileLinkRAMTest()),
        Module(new uncore.converters.TileLinkWidthAdapterTest()),
        Module(new uncore.tilelink2.TLFuzzRAMTest),
        Module(new uncore.axi4.AXI4LiteFuzzRAMTest),
        Module(new uncore.axi4.AXI4FullFuzzRAMTest),
        Module(new uncore.axi4.AXI4BridgeTest)) }
    case _ => throw new CDEMatchError
  }
)

class UncoreUnitTestConfig extends Config(new WithUncoreUnitTests ++ new BaseConfig)

class WithTLSimpleUnitTests extends Config(
  (pname, site, here) => pname match {
    case UnitTests => (q: Parameters) => {
      implicit val p = q
      Seq(
        Module(new uncore.tilelink2.TLRAMSimpleTest(1)),
        Module(new uncore.tilelink2.TLRAMSimpleTest(4)),
        Module(new uncore.tilelink2.TLRAMSimpleTest(16)),
        Module(new uncore.tilelink2.TLRR0Test),
        Module(new uncore.tilelink2.TLRR1Test),
        Module(new uncore.tilelink2.TLRAMCrossingTest) ) }
    case _ => throw new CDEMatchError })

class WithTLWidthUnitTests extends Config(
  (pname, site, here) => pname match {
    case UnitTests => (q: Parameters) => {
      implicit val p = q
      Seq(
        Module(new uncore.tilelink2.TLRAMFragmenterTest( 4, 256)),
        Module(new uncore.tilelink2.TLRAMFragmenterTest(16,  64)),
        Module(new uncore.tilelink2.TLRAMFragmenterTest( 4,  16)),
        Module(new uncore.tilelink2.TLRAMWidthWidgetTest( 1,  1)),
        Module(new uncore.tilelink2.TLRAMWidthWidgetTest( 4, 64)),
        Module(new uncore.tilelink2.TLRAMWidthWidgetTest(64,  4)) ) }
    case _ => throw new CDEMatchError })

class WithTLXbarUnitTests extends Config(
  (pname, site, here) => pname match {
    case UnitTests => (q: Parameters) => {
      implicit val p = q
      Seq(
        Module(new uncore.tilelink2.TLRAMXbarTest(1)),
        Module(new uncore.tilelink2.TLRAMXbarTest(2)),
        Module(new uncore.tilelink2.TLRAMXbarTest(8)),
        //Module(new uncore.tilelink2.TLMulticlientXbarTest(4,4)),
        Module(new uncore.tilelink2.TLMulticlientXbarTest(1,4)) ) }
    case _ => throw new CDEMatchError })

class TLSimpleUnitTestConfig extends Config(new WithTLSimpleUnitTests ++ new BasePlatformConfig)
class TLWidthUnitTestConfig extends Config(new WithTLWidthUnitTests ++ new BasePlatformConfig)
class TLXbarUnitTestConfig extends Config(new WithTLXbarUnitTests ++ new BasePlatformConfig)
