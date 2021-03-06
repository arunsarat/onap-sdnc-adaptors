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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.openecomp.sdnc.lock.comp.LockHelper;
import org.openecomp.sdnc.lock.comp.ResourceLockedException;
import org.openecomp.sdnc.lock.comp.SynchronizedFunction;
import org.openecomp.sdnc.rm.dao.ResourceDao;
import org.openecomp.sdnc.rm.data.AllocationOutcome;
import org.openecomp.sdnc.rm.data.AllocationRequest;
import org.openecomp.sdnc.rm.data.AllocationStatus;
import org.openecomp.sdnc.rm.data.LabelAllocationOutcome;
import org.openecomp.sdnc.rm.data.LabelAllocationRequest;
import org.openecomp.sdnc.rm.data.LabelResource;
import org.openecomp.sdnc.rm.data.LimitAllocationOutcome;
import org.openecomp.sdnc.rm.data.LimitAllocationRequest;
import org.openecomp.sdnc.rm.data.LimitResource;
import org.openecomp.sdnc.rm.data.MultiAssetAllocationOutcome;
import org.openecomp.sdnc.rm.data.MultiAssetAllocationRequest;
import org.openecomp.sdnc.rm.data.MultiResourceAllocationOutcome;
import org.openecomp.sdnc.rm.data.MultiResourceAllocationRequest;
import org.openecomp.sdnc.rm.data.RangeAllocationOutcome;
import org.openecomp.sdnc.rm.data.RangeAllocationRequest;
import org.openecomp.sdnc.rm.data.RangeResource;
import org.openecomp.sdnc.rm.data.Resource;
import org.openecomp.sdnc.rm.data.ResourceKey;
import org.openecomp.sdnc.rm.data.ResourceType;
import org.openecomp.sdnc.rm.util.LabelUtil;
import org.openecomp.sdnc.rm.util.LimitUtil;
import org.openecomp.sdnc.rm.util.RangeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class AllocationFunction extends SynchronizedFunction {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(AllocationFunction.class);

	private ResourceDao resourceDao;

	private String applicationId;
	private AllocationRequest request;
	private AllocationOutcome outcome;

	private List<Resource> updateList = new ArrayList<Resource>();

	public AllocationFunction(LockHelper lockHelper, ResourceDao resourceDao, String applicationId,
	        AllocationRequest request, int lockTimeout) {
		super(lockHelper, getLockNames(request), lockTimeout);
		this.applicationId = applicationId;
		this.resourceDao = resourceDao;
		this.request = request;
	}

	private static Collection<String> getLockNames(AllocationRequest request) {
		Set<String> lockResourceNames = new HashSet<String>();
		addLockNames(lockResourceNames, request);
		return lockResourceNames;
	}

	private static void addLockNames(Set<String> lockResourceNames, AllocationRequest request) {
		if (request instanceof MultiAssetAllocationRequest) {
			MultiAssetAllocationRequest req = (MultiAssetAllocationRequest) request;
			if (req.assetIdList != null)
				lockResourceNames.addAll(req.assetIdList);
		} else if (request instanceof MultiResourceAllocationRequest) {
			MultiResourceAllocationRequest req = (MultiResourceAllocationRequest) request;
			if (req.allocationRequestList != null)
				for (AllocationRequest request1 : req.allocationRequestList)
					addLockNames(lockResourceNames, request1);
		} else if (request.assetId != null)
			lockResourceNames.add(request.assetId);
	}

	@Override
	public void _exec() throws ResourceLockedException {
		outcome = allocate(request);
		if (outcome.status == AllocationStatus.Success)
			for (Resource r : updateList)
				resourceDao.saveResource(r);
	}

	private AllocationOutcome allocate(AllocationRequest allocationRequest) throws ResourceLockedException {
		if (allocationRequest instanceof MultiAssetAllocationRequest)
			return allocateMultiAsset((MultiAssetAllocationRequest) allocationRequest);
		if (allocationRequest instanceof MultiResourceAllocationRequest)
			return allocateMultiResource((MultiResourceAllocationRequest) allocationRequest);
		if (allocationRequest instanceof LimitAllocationRequest)
			return allocateLimit((LimitAllocationRequest) allocationRequest);
		if (allocationRequest instanceof LabelAllocationRequest)
			return allocateLabel((LabelAllocationRequest) allocationRequest);
		if (allocationRequest instanceof RangeAllocationRequest)
			return allocateRange((RangeAllocationRequest) allocationRequest);
		return null;
	}

	private MultiAssetAllocationOutcome allocateMultiAsset(MultiAssetAllocationRequest req) {
		// TODO Auto-generated method stub
		return null;
	}

	private MultiResourceAllocationOutcome allocateMultiResource(MultiResourceAllocationRequest req) {
		MultiResourceAllocationOutcome out = new MultiResourceAllocationOutcome();
		out.request = req;
		out.allocationOutcomeList = new ArrayList<AllocationOutcome>();
		out.status = AllocationStatus.Success;

		if (req.allocationRequestList != null)
			for (AllocationRequest req1 : req.allocationRequestList) {
				AllocationOutcome out1 = allocate(req1);
				out.allocationOutcomeList.add(out1);
				if (out1.status != AllocationStatus.Success)
					out.status = AllocationStatus.Failure;
			}

		return out;
	}

	private LimitAllocationOutcome allocateLimit(LimitAllocationRequest req) {
		LimitAllocationOutcome out = new LimitAllocationOutcome();
		out.request = req;

		Resource r = resourceDao.getResource(req.assetId, req.resourceName);
		if (r == null) {
			r = new LimitResource();
			r.resourceKey = new ResourceKey();
			r.resourceKey.assetId = req.assetId;
			r.resourceKey.resourceName = req.resourceName;
			r.resourceType = ResourceType.Limit;
		} else {
			if (r.resourceType != ResourceType.Limit) {
				out.status = AllocationStatus.ResourceNotFound;
				return out;
			}
			LimitUtil.recalculate((LimitResource) r);
		}

		LimitResource l = (LimitResource) r;
		if (LimitUtil.checkLimit(l, req)) {
			out.status = AllocationStatus.Success;
			if (req.allocateCount > 0) {
				out.allocatedCount = LimitUtil.allocateLimit(l, req, applicationId);
				updateList.add(l);
			}
		} else
			out.status = AllocationStatus.Failure;

		out.used = l.used;
		out.limit = req.checkLimit;

		return out;
	}

	private LabelAllocationOutcome allocateLabel(LabelAllocationRequest req) {
		LabelAllocationOutcome out = new LabelAllocationOutcome();

		out.request = req;

		Resource r = resourceDao.getResource(req.assetId, req.resourceName);
		if (r == null) {
			r = new LabelResource();
			r.resourceKey = new ResourceKey();
			r.resourceKey.assetId = req.assetId;
			r.resourceKey.resourceName = req.resourceName;
			r.resourceType = ResourceType.Label;
		} else {
			if (r.resourceType != ResourceType.Label) {
				out.status = AllocationStatus.ResourceNotFound;
				return out;
			}
			LabelUtil.recalculate((LabelResource) r);
		}

		LabelResource l = (LabelResource) r;
		if (LabelUtil.checkLabel(l, req)) {
			out.status = AllocationStatus.Success;
			out.currentLabel = l.label;
			if (req.allocate) {
				out.allocatedLabel = LabelUtil.allocateLabel(l, req, applicationId);
				updateList.add(l);
			}
		} else
			out.status = AllocationStatus.Failure;

		return out;
	}

	private RangeAllocationOutcome allocateRange(RangeAllocationRequest req) {
		RangeAllocationOutcome out = new RangeAllocationOutcome();

		out.request = req;

		Resource r = resourceDao.getResource(req.assetId, req.resourceName);
		if (r == null) {
			r = new RangeResource();
			r.resourceKey = new ResourceKey();
			r.resourceKey.assetId = req.assetId;
			r.resourceKey.resourceName = req.resourceName;
			r.resourceType = ResourceType.Range;
		} else {
			if (r.resourceType != ResourceType.Range) {
				out.status = AllocationStatus.ResourceNotFound;
				return out;
			}
			RangeUtil.recalculate((RangeResource) r);
		}

		RangeResource rr = (RangeResource) r;
		SortedSet<Integer> foundNumbers = null;
		if (!req.check) {
			out.status = AllocationStatus.Success;
			foundNumbers = req.requestedNumbers;
		} else {
			if (req.requestedNumbers != null && req.requestedNumbers.size() > 0) {
				foundNumbers = req.requestedNumbers;
				out.status = AllocationStatus.Success;
				for (int n : foundNumbers)
					if (!RangeUtil.checkRange(rr, req, n)) {
						out.status = AllocationStatus.Failure;
						break;
					}
			} else {
				foundNumbers = new TreeSet<Integer>();
				int foundCount = 0;

				// First try to reuse the numbers already taken by the same resource union
				SortedSet<Integer> uu = RangeUtil.getUsed(rr, req.resourceUnionId);
				if (uu != null && !uu.isEmpty()) {
					if (uu.size() >= req.requestedCount) {
						// Just take the first req.requestedCount numbers from uu
						Iterator<Integer> i = uu.iterator();
						while (foundCount < req.requestedCount) {
							foundNumbers.add(i.next());
							foundCount++;
						}
					} else {
						// Additional numbers are requested. Try to find them starting from
						// the minimum we have in uu (the first element) towards the min
						// parameter, and then starting from the maximum in uu (the last
						// element) towards the max parameter.
						// NOTE: In case of request for sequential numbers, the parameters
						// alignBlockSize and alignModulus are ignored. It would be harder
						// to take them into account, and currently it is not needed.

						int uumin = uu.first() - 1;
						int uumax = uu.last() + 1;
						foundNumbers.addAll(uu);
						foundCount = uu.size();
						for (int n = uumin; foundCount < req.requestedCount && n >= req.checkMin; n--) {
							if (RangeUtil.checkRange(rr, req, n)) {
								foundNumbers.add(n);
								foundCount++;
							} else if (req.sequential)
								break;
						}
						for (int n = uumax; foundCount < req.requestedCount && n <= req.checkMax; n++) {
							if (RangeUtil.checkRange(rr, req, n)) {
								foundNumbers.add(n);
								foundCount++;
							} else if (req.sequential)
								break;
						}

						// If we could not find enough numbers trying to reuse currently
						// allocated, reset foundNumbers and foundCount, continue with
						// the normal allocation of new numbers.
						if (foundCount < req.requestedCount) {
							foundNumbers = new TreeSet<Integer>();
							foundCount = 0;
						}
					}
				}

				for (int n = req.checkMin; foundCount < req.requestedCount && n <= req.checkMax; n++)
					if (RangeUtil.checkRange(rr, req, n)) {
						foundNumbers.add(n);
						foundCount++;
					} else if (req.sequential)
						foundCount = 0;

				out.status = foundCount == req.requestedCount ? AllocationStatus.Success : AllocationStatus.Failure;
			}
		}

		if (out.status == AllocationStatus.Success) {
			out.allocated = foundNumbers;
			if (req.allocate) {
				RangeUtil.allocateRange(rr, out.allocated, req, applicationId);
				updateList.add(rr);
			}
		} else
			out.allocated = new TreeSet<Integer>();

		out.used = rr.used;

		return out;
	}

	public AllocationOutcome getAllocationOutcome() {
		return outcome;
	}
}
