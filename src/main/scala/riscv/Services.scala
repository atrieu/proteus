package riscv

import riscv._

import spinal.core._

trait IBusService {
  def getIBus: MemBus
}

trait DBusService {
  def getDBus: MemBus
}

trait DecoderService {
  type Action = Map[PipelineData[_ <: Data], Data]

  trait DecoderConfig {
    def addDecoding(opcode: MaskedLiteral,
                    itype: InstructionType,
                    action: Action): Unit
    def addDecoding(opcode: MaskedLiteral, action: Action): Unit
    def addDefault(action: Action): Unit
    def addDefault(data: PipelineData[_ <: Data], value: Data): Unit
  }

  protected val decoderConfig: DecoderConfig
  protected def stage(pipeline: Pipeline): Stage

  def configure(pipeline: Pipeline)(f: DecoderConfig => Unit): Unit = {
    stage(pipeline).rework(f(decoderConfig))
  }
}

trait IntAluService {
  object AluOp extends SpinalEnum {
    val ADD, SUB, SLT, SLTU, XOR, OR, AND, SRC2 = newElement()
  }

  object Src1Select extends SpinalEnum {
    val RS1, PC = newElement()
  }

  object Src2Select extends SpinalEnum {
    val RS2, IMM = newElement()
  }

  def addOperation(pipeline: Pipeline, opcode: MaskedLiteral,
                   op: SpinalEnumElement[AluOp.type],
                   src1: SpinalEnumElement[Src1Select.type],
                   src2: SpinalEnumElement[Src2Select.type]): Unit

  def resultData: PipelineData[UInt]
}

trait JumpService {
  def jump(pipeline: Pipeline, stage: Stage, target: UInt): Unit
}

trait Csr extends Area {
  def read(): UInt
  def write(value: UInt): Unit = assert(false, "Cannot write RO CSR")
}

trait CsrService {
    def registerCsr[T <: Csr](pipeline: Pipeline, id: Int, reg: => T): T
}

trait FormalService {
  def lsuDefault(stage: Stage)
  def lsuOnLoad(stage: Stage, addr: UInt, rmask: Bits, rdata: UInt)
  def lsuOnStore(stage: Stage, addr: UInt, wmask: Bits, wdata: UInt)
  def lsuOnMisaligned(stage: Stage): Unit
}
