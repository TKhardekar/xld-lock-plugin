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

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import com.xebialabs.deployit.booter.local.LocalBooter;
import com.xebialabs.deployit.plugin.api.udm.ConfigurationItem;
import com.xebialabs.deployit.plugin.api.udm.base.BaseContainer;

public class LockFileHelperTest {

	private ConfigurationItem ci;
	private ConfigurationItem ci2;
	private LockHelper lockHelper = new LockHelper();

	@Before
	public void createContainer() {
        LocalBooter.bootWithoutGlobalContext();
        this.ci = new BaseContainer();
		this.ci.setId("Infrastructure/lock/TestContainer");
		this.ci2 = new BaseContainer();
		this.ci2.setId("Infrastructure/lock/TestContainer2");
	}

	@Before
	public void clearLockDirectory() {
		lockHelper.clearLocks();
	}

	@Test
	public void shouldCorrectlyConvertCiIdToLockFileNameAndBack() throws FileNotFoundException {
		String lockFileName = lockHelper.ciIdToLockFileName(ci.getId());
		assertThat(ci.getId(), is(equalTo(lockHelper.lockFileNameToCiId(lockFileName))));
	}

	@Test
	public void shouldCorrectlyLockContainer() throws IOException {
		assertThat(lockHelper.isLocked(ci), is(equalTo(false)));
		
		assertThat(lockHelper.lock(ci), is(equalTo(true)));
		assertThat(lockHelper.isLocked(ci), is(equalTo(true)));
	}

	@Test
	public void shouldNotAllowLockIfAlreadyLocked() throws IOException {
		assertThat(lockHelper.isLocked(ci), is(equalTo(false)));
		
		assertThat(lockHelper.lock(ci), is(equalTo(true)));		
		assertThat(lockHelper.isLocked(ci), is(equalTo(true)));
		
		assertThat(lockHelper.lock(ci), is(equalTo(false)));
	}

	@Test
	public void shouldCorrectlyClearLocks() throws IOException {
		assertThat(lockHelper.lock(ci), is(equalTo(true)));		

		lockHelper.clearLocks();
		
		assertThat(lockHelper.isLocked(ci), is(equalTo(false)));

		assertThat(lockHelper.lock(ci), is(equalTo(true)));		
	}

	@Test
	public void shouldCorrectlyLockAllCis() throws IOException {
		assertThat(lockHelper.atomicallyLock(Lists.newArrayList(ci, ci2)), is(equalTo(true)));		

		assertThat(lockHelper.isLocked(ci), is(equalTo(true)));
		assertThat(lockHelper.isLocked(ci2), is(equalTo(true)));
	}
	
	@Test
	public void shouldReleaseLocksWhenAtomicLockingFails() throws IOException {
		assertThat(lockHelper.lock(ci2), is(equalTo(true)));		
		assertThat(lockHelper.atomicallyLock(Lists.newArrayList(ci, ci2)), is(equalTo(false)));		

		assertThat(lockHelper.isLocked(ci), is(equalTo(false)));
		assertThat(lockHelper.isLocked(ci2), is(equalTo(true)));
	}
}
