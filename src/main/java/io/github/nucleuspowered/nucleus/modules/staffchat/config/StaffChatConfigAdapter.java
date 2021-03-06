/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.staffchat.config;

import com.google.common.reflect.TypeToken;
import io.github.nucleuspowered.nucleus.internal.qsml.NucleusConfigAdapter;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

public class StaffChatConfigAdapter extends NucleusConfigAdapter<StaffChatConfig> {

    @Override
    protected StaffChatConfig getDefaultObject() {
        return new StaffChatConfig();
    }

    @Override
    protected StaffChatConfig convertFromConfigurateNode(ConfigurationNode node) throws ObjectMappingException {
        return node.getValue(TypeToken.of(StaffChatConfig.class), new StaffChatConfig());
    }

    @Override
    protected ConfigurationNode insertIntoConfigurateNode(StaffChatConfig data) throws ObjectMappingException {
        return SimpleCommentedConfigurationNode.root().setValue(TypeToken.of(StaffChatConfig.class), data);
    }
}
