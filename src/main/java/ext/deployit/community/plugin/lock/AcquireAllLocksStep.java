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

import java.util.Set;

import com.xebialabs.deployit.plugin.api.flow.ExecutionContext;
import com.xebialabs.deployit.plugin.api.flow.Step;
import com.xebialabs.deployit.plugin.api.flow.StepExitCode;
import com.xebialabs.deployit.plugin.api.udm.ConfigurationItem;

public class AcquireAllLocksStep implements Step {

	private static final int ACQUIRE_LOCKS_ORDER = 2;
	private final Set<ConfigurationItem> cisToBeLocked;
	private boolean enableRetry;
	private int retryInSeconds;
	private int retryLimit;
	private final LockHelper lockHelper;

	public AcquireAllLocksStep(LockHelper lockHelper, Set<ConfigurationItem> cisToBeLocked, boolean enableRetry, int retryInSeconds, int retryLimit) {
		this.lockHelper = lockHelper;
		this.cisToBeLocked = cisToBeLocked;
		this.enableRetry = enableRetry;
		this.retryInSeconds = retryInSeconds;
		this.retryLimit = retryLimit;
	}

	@Override
	public StepExitCode execute(ExecutionContext context) throws Exception {
		context.logOutput("Attempting to acquire locks on CIs " + cisToBeLocked);


		boolean locked = lockHelper.atomicallyLock(cisToBeLocked);
		if (!locked && enableRetry) {
			int retryCount = 0;
			while(!locked && retryCount < retryLimit ) {
				context.logOutput("Will retry in "+ retryInSeconds +" seconds");
				Thread.sleep(retryInSeconds*1000);
				context.logOutput("Attempting to acquire locks on CIs " + cisToBeLocked);
				locked = lockHelper.atomicallyLock(cisToBeLocked);
				retryCount++;
			}
		}
		if (locked) {
			context.logOutput("All locks acquired");
			context.setAttribute("lockCleanupListener", new LockCleanupListener(lockHelper, cisToBeLocked));
			return StepExitCode.SUCCESS;
		} else {
			context.logError("Failed to acquire one or more locks");
			return StepExitCode.PAUSE;
		}
	}

	@Override
	public String getDescription() {
		return "Acquiring locks for the following CIs: " + cisToBeLocked.toString();
	}

	@Override
	public int getOrder() {
		return ACQUIRE_LOCKS_ORDER;
	}

}
