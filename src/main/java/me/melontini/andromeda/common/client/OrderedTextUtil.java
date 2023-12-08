package me.melontini.andromeda.common.client;

import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.CharacterVisitor;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import java.util.List;

public class OrderedTextUtil {

    public static List<Text> wrap(Text text, int length) {
        return MinecraftClient.getInstance().textRenderer.wrapLines(text, length)
                .stream().map(OrderedTextUtil::orderedToNormal).toList();
    }

    //MIT License
    //
    //Copyright (c) 2019 kyrptonaught
    //
    //Permission is hereby granted, free of charge, to any person obtaining a copy
    //of this software and associated documentation files (the "Software"), to deal
    //in the Software without restriction, including without limitation the rights
    //to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    //copies of the Software, and to permit persons to whom the Software is
    //furnished to do so, subject to the following conditions:
    //
    //The above copyright notice and this permission notice shall be included in all
    //copies or substantial portions of the Software.
    //
    //THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    //IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    //FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    //AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    //LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    //OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    //SOFTWARE.
    public static Text orderedToNormal(OrderedText orderedText) {
        MutableText text = TextUtil.empty();
        CharacterVisitor characterVisitor = (index, style, codePoint) -> {
            String str = new String(Character.toChars(codePoint));
            text.append(TextUtil.literal(str).setStyle(style));
            return true;
        };
        orderedText.accept(characterVisitor);
        return text;
    }
}
