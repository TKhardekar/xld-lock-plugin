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

import java.io.Serializable;
import java.util.Set;

import com.xebialabs.deployit.engine.spi.execution.ExecutionStateListener;
import com.xebialabs.deployit.engine.spi.execution.StepExecutionStateEvent;
import com.xebialabs.deployit.engine.spi.execution.TaskExecutionStateEvent;
import static com.xebialabs.deployit.engine.api.execution.TaskExecutionState.*;
import com.xebialabs.deployit.plugin.api.udm.ConfigurationItem;

public class LockCleanupListener implements ExecutionStateListener, Serializable {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(LockCleanupListener.class);

	private final LockHelper lockHelper;
	private final Set<ConfigurationItem> cisToBeUnlocked;

	public LockCleanupListener(LockHelper lockHelper, Set<ConfigurationItem> cisToBeUnlocked) {
		this.lockHelper = lockHelper;
		this.cisToBeUnlocked = cisToBeUnlocked;
	}
	
	@Override
	public void stepStateChanged(StepExecutionStateEvent event) {

		logger.info("Step event: {}", event);
	}

	@Override
	public void taskStateChanged(TaskExecutionStateEvent event) {
		logger.info("Task event: {}", event);

		switch(event.currentState()) {
			case CANCELLED:
			case DONE:
				lockHelper.unlock(cisToBeUnlocked);
				break;
			default:
				break;
		}
	}

}
