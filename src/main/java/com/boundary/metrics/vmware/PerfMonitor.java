package com.boundary.metrics.vmware;

import java.io.IOException;
import java.net.URISyntaxException;

import com.boundary.metrics.vmware.poller.VMwareClient;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.PerfProviderSummary;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.VimPortType;
import com.vmware.vim25.VimService;

public class PerfMonitor {
	
	public static void main(String [] args) throws URISyntaxException, IOException, InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
		VMwareClient client = VMWareClientFactory.createClient();
		client.connect();
		VimService vimService = client.getVimService();
		VimPortType vimPort = vimService.getVimPort();
		
		ManagedObjectReference pm = client.getServiceContent().getPerfManager();
		String vmName = "RHEL-TestVM01";
		ManagedObjectReference mor = client.getVMByName(vmName);
		
		PerfProviderSummary perf = vimPort.queryPerfProviderSummary(pm, mor);
		System.out.println(perf.getRefreshRate());
		
	}
}
