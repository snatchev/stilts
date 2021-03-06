/*
 * Copyright 2011 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.projectodd.stilts.stomp.server.protocol;

import org.jboss.logging.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.projectodd.stilts.stomp.TransactionalAcknowledger;
import org.projectodd.stilts.stomp.protocol.StompFrame;
import org.projectodd.stilts.stomp.protocol.StompFrame.Command;
import org.projectodd.stilts.stomp.protocol.StompFrame.Header;
import org.projectodd.stilts.stomp.spi.StompProvider;

public class AckHandler extends AbstractControlFrameHandler {

    private static final Logger log = Logger.getLogger( AckHandler.class );

    public AckHandler(StompProvider provider, ConnectionContext context) {
        super( provider, context, Command.ACK );
    }

    public void handleControlFrame(ChannelHandlerContext channelContext, StompFrame frame) {
        String messageId = frame.getHeader( Header.MESSAGE_ID );
        TransactionalAcknowledger acknowledger = getContext().getAckManager().removeAcknowledger( messageId );
        if (acknowledger != null) {
            String transactionId = frame.getHeader( Header.TRANSACTION );
            try {
                acknowledger.ack( transactionId );
            } catch (Exception e) {
                log.errorf(e, "Cannot ack frame: %s", frame);
                sendError( channelContext, "Unable to ACK", frame );
            }
        }
    }

}
