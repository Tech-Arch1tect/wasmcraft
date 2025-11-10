package uk.co.techarchitect.wasmcraft.computer;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;

public class Terminal {
    private final List<String> outputHistory = new ArrayList<>();
    private final int maxLines;

    public Terminal(int maxLines) {
        this.maxLines = maxLines;
    }

    public void addLine(String line) {
        outputHistory.add(line);
        while (outputHistory.size() > maxLines) {
            outputHistory.remove(0);
        }
    }

    public void clear() {
        outputHistory.clear();
    }

    public List<String> getHistory() {
        return new ArrayList<>(outputHistory);
    }

    public void saveToNbt(ListTag tag) {
        for (String line : outputHistory) {
            tag.add(StringTag.valueOf(line));
        }
    }

    public void loadFromNbt(ListTag tag) {
        outputHistory.clear();
        for (int i = 0; i < tag.size(); i++) {
            addLine(tag.getString(i));
        }
    }
}
