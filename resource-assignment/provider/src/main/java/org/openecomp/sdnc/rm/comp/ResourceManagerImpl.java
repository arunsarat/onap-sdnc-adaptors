/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 							reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdnc.rm.comp;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openecomp.sdnc.lock.comp.LockHelper;
import org.openecomp.sdnc.rm.dao.ResourceDao;
import org.openecomp.sdnc.rm.data.AllocationOutcome;
import org.openecomp.sdnc.rm.data.AllocationRequest;
import org.openecomp.sdnc.rm.data.Resource;
import org.openecomp.sdnc.rm.util.ResourceUtil;
import org.openecomp.sdnc.util.str.StrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceManagerImpl implements ResourceManager {

	private static final Logger log = LoggerFactory.getLogger(ResourceManagerImpl.class);

	private LockHelper lockHelper;
	private ResourceDao resourceDao;

	private String applicationId;
	private int lockTimeout = 10 * 60; // Default 10 min

	public ResourceManagerImpl() {
		log.info("ResourceManager created.");
	}

	@Override
	public Resource getResource(String resourceName, String assetId) {
		Resource r = resourceDao.getResource(assetId, resourceName);
		ResourceUtil.recalculate(r);
		return r;
	}

	@Override
	public List<Resource> getResourceUnion(String resourceUnionId) {
		List<Resource> rlist = resourceDao.getResourceUnion(resourceUnionId);
		for (Resource r : rlist)
			ResourceUtil.recalculate(r);
		return rlist;
	}

	@Override
	public AllocationOutcome allocateResources(AllocationRequest allocationRequest) {
		if (allocationRequest == null)
			throw new IllegalArgumentException("allocateResources called with null argument");

		AllocationFunction allocationFunction =
		        new AllocationFunction(lockHelper, resourceDao, applicationId, allocationRequest, lockTimeout);
		allocationFunction.exec();
		AllocationOutcome allocationOutcome = allocationFunction.getAllocationOutcome();

		StrUtil.info(log, allocationOutcome);

		return allocationOutcome;
	}

	@Override
	public void releaseResourceSet(String resourceSetId) {
		List<Resource> resourceList = resourceDao.getResourceSet(resourceSetId);
		if (resourceList == null || resourceList.isEmpty())
			return;

		Set<String> lockNames = getLockNames(resourceList);
		ReleaseFunction releaseFunction =
		        new ReleaseFunction(lockHelper, resourceDao, resourceSetId, null, lockNames, lockTimeout);
		releaseFunction.exec();
	}

	@Override
	public void releaseResourceUnion(String resourceUnionId) {
		List<Resource> resourceList = resourceDao.getResourceUnion(resourceUnionId);
		if (resourceList == null || resourceList.isEmpty())
			return;

		Set<String> lockNames = getLockNames(resourceList);
		ReleaseFunction releaseFunction =
		        new ReleaseFunction(lockHelper, resourceDao, null, resourceUnionId, lockNames, lockTimeout);
		releaseFunction.exec();
	}

	private Set<String> getLockNames(List<Resource> resourceList) {
		Set<String> lockNames = new HashSet<String>();
		for (Resource r : resourceList)
			lockNames.add(r.resourceKey.assetId);
		return lockNames;
	}

	public void setResourceDao(ResourceDao resourceDao) {
		this.resourceDao = resourceDao;
	}

	public void setLockTimeout(int lockTimeout) {
		this.lockTimeout = lockTimeout;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	public void setLockHelper(LockHelper lockHelper) {
		this.lockHelper = lockHelper;
	}
}
