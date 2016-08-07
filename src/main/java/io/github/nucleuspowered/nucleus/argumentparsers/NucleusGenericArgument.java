/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.argumentparsers;

import com.google.common.collect.ImmutableMap;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Tristate;

import java.util.Map;

public class NucleusGenericArgument {

    private NucleusGenericArgument() {}

    private static Map<String, Tristate> stringTristateMap =
            ImmutableMap.<String, Tristate>builder().put("true", Tristate.TRUE)
                    .put("t", Tristate.TRUE).put("1", Tristate.TRUE)
                    .put("false", Tristate.FALSE).put("f", Tristate.FALSE)
                    .put("0", Tristate.FALSE).put("unknown", Tristate.UNDEFINED)
                    .put("undefined", Tristate.UNDEFINED).put("default", Tristate.UNDEFINED).build();

    public static CommandElement tristateChoice(Text key) {
        return GenericArguments.choices(key, stringTristateMap);
    }
}
