/*
 * Copyright 2019 dc-square GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hivemq.mqtt.handler.auth;

import com.google.common.annotations.VisibleForTesting;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.mqtt.handler.connack.MqttConnacker;
import com.hivemq.mqtt.message.auth.AUTH;
import com.hivemq.mqtt.message.disconnect.DISCONNECT;
import com.hivemq.mqtt.message.reason.Mqtt5ConnAckReasonCode;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.hivemq.util.ChannelUtils.getClientId;

/**
 * @author Georg Held
 */
@ChannelHandler.Sharable
@Singleton
public class AuthInProgressMessageHandler extends ChannelInboundHandlerAdapter {

    @NotNull
    private static final String DISCONNECT_LOG_MESSAGE = "The client with id %s and IP {} sent a non " +
            "AUTH non DISCONNECT message during enhanced authentication. " +
            "This is not allowed. Disconnecting client.";

    @NotNull
    private final MqttConnacker connacker;

    @VisibleForTesting
    @Inject
    public AuthInProgressMessageHandler(@NotNull final MqttConnacker connacker) {
        this.connacker = connacker;
    }

    @Override
    public void channelRead(@NotNull final ChannelHandlerContext ctx, @NotNull final Object msg) throws Exception {

        if (msg instanceof AUTH || msg instanceof DISCONNECT) {
            super.channelRead(ctx, msg);
            return;
        }

        connacker.connackErrorMqtt5(ctx.channel(),
                String.format(DISCONNECT_LOG_MESSAGE, getClientId(ctx.channel())),
                "Sent non AUTH non DISCONNECT message",
                Mqtt5ConnAckReasonCode.PROTOCOL_ERROR,
                "non AUTH non DISCONNECT message");
    }
}