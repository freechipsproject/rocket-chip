// See LICENSE.SiFive for license details.

#include <vpi_user.h>
#include <sv_vpi_user.h>
//#include <svdpi.h>
#include <cstdlib>
#include "remote_bitbang.h"

remote_bitbang_t* jtag;

static PLI_INT32 jtag_tick_calltf(PLI_BYTE8 *nouse)
{
  unsigned char * jtag_TCK;   //0
  unsigned char * jtag_TMS;   //1
  unsigned char * jtag_TDI;   //2
  unsigned char * jtag_TRSTn; //3
  unsigned char jtag_TDO;     //4
  
  vpiHandle sys = vpi_handle(vpiSysTfCall, 0);
  vpiHandle argv = vpi_iterate(vpiArgument, sys);
  vpiHandle arg[5];
  s_vpi_value val[5];
  s_vpi_value rtn;
  
  // link all signals
  for(int i = 0; i < 5; i++) {
    arg[i] = vpi_scan(argv);
    val[i].format = vpiIntVal;
  }
  vpi_free_object(argv);

  if (!jtag) {
    s_vpi_vlog_info info;
    if (!vpi_get_vlog_info(&info)) {
      abort();
    }
    // TODO: Pass in real port number
    jtag = new remote_bitbang_t(0);
  }
  
  // Get value
  vpi_get_value(arg[4], &(val[4]));
  jtag_TDO = val[4].value.integer;

  jtag->tick(jtag_TCK, jtag_TMS, jtag_TDI, jtag_TRSTn, jtag_TDO);

  // Put value
  val[0].value.integer = *jtag_TCK;
  val[1].value.integer = *jtag_TMS;
  val[2].value.integer = *jtag_TDI;
  val[3].value.integer = *jtag_TRSTn;

  vpi_put_value(arg[0], &(val[0]), 0, vpiNoDelay); 
  vpi_put_value(arg[1], &(val[1]), 0, vpiNoDelay); 
  vpi_put_value(arg[2], &(val[2]), 0, vpiNoDelay); 
  vpi_put_value(arg[3], &(val[3]), 0, vpiNoDelay); 

  // return value
  rtn.format = vpiIntVal;
  rtn.value.integer = jtag->done() ? (jtag->exit_code() << 1 | 1) : 0;
  vpi_put_value(sys, &rtn, 0, vpiNoDelay);
  return 0;

}

PLI_INT32 jtag_tick_sizetf(PLI_BYTE8 *nouse)
{
  return 32;
}

void jtag_tick_register(void)
{
      s_vpi_systf_data tf_data;

      tf_data.type        = vpiSysFunc;
      tf_data.sysfunctype = vpiIntFunc;
      tf_data.tfname    = "$jtag_tick";
      tf_data.calltf    = jtag_tick_calltf;
      tf_data.sizetf    = 0; //jtag_tick_sizetf;
      tf_data.compiletf = 0;
      vpi_register_systf(&tf_data);
}
