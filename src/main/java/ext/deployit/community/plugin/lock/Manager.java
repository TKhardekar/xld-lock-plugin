/**
 * Copyright 2019 XEBIALABS
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ext.deployit.community.plugin.lock;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import com.xebialabs.deployit.plugin.api.flow.ExecutionContext;
import com.xebialabs.deployit.plugin.api.flow.Step;
import com.xebialabs.deployit.plugin.api.flow.StepExitCode;
import com.xebialabs.deployit.plugin.api.udm.ControlTask;
import com.xebialabs.deployit.plugin.api.udm.Metadata;
import com.xebialabs.deployit.plugin.api.udm.base.BaseContainer;

/**
 * Lock manager CI that provides control tasks to list and clear locks.
 */
@SuppressWarnings("serial")
@Metadata(root = Metadata.ConfigurationItemRoot.INFRASTRUCTURE, virtual = false, description = "Manager for container locks")
public class Manager extends BaseContainer {

	@SuppressWarnings("rawtypes")
    @ControlTask(description="Clears all locks")
	public List<Step> clearLocks() {
		Step clearLocksStep = new Step() {

			@Override
			public String getDescription() {
				return "Clearing all locks";
			}

			@Override
			public StepExitCode execute(ExecutionContext arg0) throws Exception {
				new LockHelper().clearLocks();
				return StepExitCode.SUCCESS;
			}

			@Override
			public int getOrder() {
				return 0;
			}
		};
		
		return newArrayList(clearLocksStep);
	}
	
	@SuppressWarnings("rawtypes")
    @ControlTask(description="Lists all locks")
	public List<Step> listLocks() {
		Step listLocksStep = new Step() {

			@Override
			public String getDescription() {
				return "Listing all locks";
			}

			@Override
			public StepExitCode execute(ExecutionContext ctx) throws Exception {
				ctx.logOutput("The following CIs are currently locked:");

				List<String> locksListing = new LockHelper().listLocks();
				if (locksListing.isEmpty()) {
					ctx.logOutput("<none>");
				} else {
					for (String string : locksListing) {
						ctx.logOutput("- " + string);
					}
				}
				
				return StepExitCode.SUCCESS;
			}

			@Override
			public int getOrder() {
				return 0;
			}
		};
		
		return newArrayList(listLocksStep);
	}
}
