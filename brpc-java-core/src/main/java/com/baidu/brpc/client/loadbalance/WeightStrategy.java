/*
 * Copyright (c) 2018 Baidu, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.baidu.brpc.client.loadbalance;

import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import com.baidu.brpc.client.RpcClient;
import com.baidu.brpc.client.channel.BrpcChannelGroup;

/**
 * Simple weight load balance strategy implementation
 * The channelGroup which has less failedNum will have the more probability
 */
public class WeightStrategy implements LoadBalanceStrategy {

    private Random random = new Random(System.currentTimeMillis());

    @Override
    public void init(RpcClient rpcClient) {

    }

    @Override
    public BrpcChannelGroup selectInstance(CopyOnWriteArrayList<BrpcChannelGroup> instances) {
        long instanceNum = instances.size();
        if (instanceNum == 0) {
            return null;
        }

        long sum = 0;
        for (BrpcChannelGroup instance : instances) {
            sum += getWeight(instance.getFailedNum());
        }
        long randWeight = random.nextLong() % sum;
        for (BrpcChannelGroup channelGroup : instances) {
            randWeight -= getWeight(channelGroup.getFailedNum());
            if (randWeight <= 0) {
                return channelGroup;
            }
        }

        return null;
    }

    @Override
    public void destroy() {
    }

    private long getWeight(long failedNum) {
        return 1000000000 / (failedNum + 1);
    }
}
