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

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Sets.newHashSet;
import static java.lang.Boolean.FALSE;

import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.xebialabs.deployit.plugin.api.deployment.planning.Contributor;
import com.xebialabs.deployit.plugin.api.deployment.planning.DeploymentPlanningContext;
import com.xebialabs.deployit.plugin.api.deployment.specification.Delta;
import com.xebialabs.deployit.plugin.api.deployment.specification.Deltas;
import com.xebialabs.deployit.plugin.api.deployment.specification.Operation;
import com.xebialabs.deployit.plugin.api.udm.ConfigurationItem;
import com.xebialabs.deployit.plugin.api.udm.Container;
import com.xebialabs.deployit.plugin.api.udm.DeployedApplication;
import com.xebialabs.deployit.plugin.api.udm.Environment;

/**
 * Write all in Java, it is cross-platform Create lock.Manager CI that can list logs and clear all locks (control tasks) Default is to use locking for each host
 * (find hostcontainer), can be turned off with synthetic property
 */
public class DeploymentLockContributor {
	private static final String CONCURRENT_DEPLOYMENTS_ALLOWED_PROPERTY = "allowConcurrentDeployments";

	@Contributor
	public static void addDeploymentLockCheckStep(Deltas deltas, DeploymentPlanningContext ctx) {
		DeployedApplication deployedApplication = ctx.getDeployedApplication();
		Environment environment = deployedApplication.getEnvironment();

		Set<ConfigurationItem> cisToBeLocked = new HashSet<ConfigurationItem>();

		// Check whether locking is required:
		//
		// 1. on the DeployedApplication
		// 2. on the Environment
		// 3. on the individual containers
		if (shouldLockCI(deployedApplication)) {
			cisToBeLocked.add(deployedApplication);
		}
		
		if (shouldLockCI(environment)) {
			cisToBeLocked.add(environment);
			boolean lockAllContainersInEnvironment = environment.getProperty("lockAllContainersInEnvironment");
			if (lockAllContainersInEnvironment) {
				for (Container container : environment.getMembers()) {
					cisToBeLocked.add(container);
				}
			}
		}

		cisToBeLocked.addAll(getContainersRequiringCheck(deltas));
		
		if (!cisToBeLocked.isEmpty()) {
			boolean enableLockRetry = environment.getProperty("enableLockRetry");
			int lockRetryInterval = environment.getProperty("lockRetryInterval");
			int lockRetryAttempts = environment.getProperty("lockRetryAttempts");
			ctx.addStep(new AcquireAllLocksStep(new LockHelper(), cisToBeLocked, enableLockRetry, lockRetryInterval, lockRetryAttempts));
		}
	}

	private static boolean shouldLockCI(ConfigurationItem ci) {
		return ci.hasProperty(CONCURRENT_DEPLOYMENTS_ALLOWED_PROPERTY) &&
				FALSE.equals(ci.getProperty(CONCURRENT_DEPLOYMENTS_ALLOWED_PROPERTY));
	}

	private static Set<Container> getContainersRequiringCheck(Deltas deltas) {
		Iterable<Container> containersInAction = transform(deltas.getDeltas(), new Function<Delta, Container>() {
			@Override
			public Container apply(Delta input) {
				return (input.getOperation() == Operation.DESTROY ? input.getPrevious().getContainer() : input.getDeployed().getContainer());
			}
		});

		HashSet<Container> containers = newHashSet(filter(containersInAction, new Predicate<Container>() {
			@Override
			public boolean apply(Container input) {
				// may be null
				return FALSE.equals(input.getProperty(CONCURRENT_DEPLOYMENTS_ALLOWED_PROPERTY));
			}
		}));
		return containers;
	}
}
