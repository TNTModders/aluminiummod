package com.tntmodders.tutorial;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class AluminiumRecipeHolder {
    //ItemStackよりそのアイテムが鍵となるレシピを取得できるようにする。
    public static final Map<ItemStack, List<ResourceLocation>> map = new ItemStackHashMap();

    public void register() {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            //assets/<modid>/recipes/よりリソースを取得する。
            this.getResource("assets/aluminiummod/recipes/");
        }
    }

    public void getResource(String path) {
        ClassLoader loader = AluminiumMod.class.getClassLoader();
        URL url = loader.getResource(path);
        //jarファイル内か否かで処理が変化する。
        if (url.getProtocol().equals("jar")) {
            String[] strings = url.getPath().split(":");
            String leadPath = strings[strings.length - 1].split("!")[0];
            File f = new File(leadPath);
            JarFile jarFile;
            try {
                //jarファイル自体を取得する。(zipファイル・jarファイルとして扱う事ができる。)
                jarFile = new JarFile(f);
                Enumeration<JarEntry> enumeration = jarFile.entries();
                while (enumeration.hasMoreElements()) {
                    JarEntry entry = enumeration.nextElement();
                    String s = entry.getName();
                    if (s != null && s.startsWith(path) && s.endsWith(".json")) {
                        InputStream stream;
                        try {
                            stream = loader.getResourceAsStream(s);
                            //inputstreamを使ってjarファイル内のjsonを読み込む。
                            this.readStream(stream, s);
                            stream.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            List<File> list = this.getListFile(path);
            if (list.size() > 0) {
                for (File recipe : list) {
                    InputStream stream;
                    try {
                        stream = new FileInputStream(recipe);
                        this.readStream(stream, recipe.getName());
                        stream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void readStream(InputStream stream, String name) {
        //inputstreamよりJSONを読み込む。
        JsonReader reader = new JsonReader(new InputStreamReader(stream));
        JsonObject jsonObject = new Gson().fromJson(reader, JsonObject.class);
        //文字列の中にパスが紛れ込んだ場合それを消す。また、".json"を抜いてResourceLocationとして保存する。
        ResourceLocation location = new ResourceLocation("aluminiummod", name.replaceAll("assets/aluminiummod/recipes/", "")
                .replaceAll(".json", ""));

        //定形レシピでキーを"#"にしたアイテムを鍵とする。
        if (jsonObject.has("key") && jsonObject.getAsJsonObject("key").has("#")) {
            Item item = Item.getByNameOrId(jsonObject.getAsJsonObject("key").getAsJsonObject("#").get("item").getAsString());
            int i = 0;
            if (jsonObject.getAsJsonObject("key").getAsJsonObject("#").has("data")) {
                i = jsonObject.getAsJsonObject("key").getAsJsonObject("#").get("data").getAsInt();
            }
            ItemStack stack = new ItemStack(item, 1, i);
            List<ResourceLocation> locations = map.containsKey(stack) ? map.get(stack) : new ArrayList<>();
            locations.add(location);
            map.put(stack, locations);
        }
        //不定形レシピで一番上に書いたアイテムを鍵とする。
        else if (jsonObject.has("ingredients") && jsonObject.getAsJsonArray("ingredients").get(0).getAsJsonObject().has("item")) {
            String s = jsonObject.getAsJsonArray("ingredients").get(0).getAsJsonObject().get("item").getAsString();
            Item item = Item.getByNameOrId(s);
            int i = 0;
            if (jsonObject.getAsJsonArray("ingredients").get(0).getAsJsonObject().has("data")) {
                i = jsonObject.getAsJsonArray("ingredients").get(0).getAsJsonObject().get("data").getAsInt();
            }
            ItemStack stack = new ItemStack(item, 1, i);
            List<ResourceLocation> locations = map.containsKey(stack) ? map.get(stack) : new ArrayList<>();
            locations.add(location);
            map.put(stack, locations);
        }
    }

    //ファイルを全て取得する。これもjarか否かによって変わる。
    private List<File> getListFile(String path) {
        List<File> files = new ArrayList<>();
        ClassLoader loader = AluminiumMod.class.getClassLoader();
        URL url = loader.getResource(path);
        if (url.getProtocol().equals("jar")) {
            String[] strings = url.getPath().split(":");
            String leadPath = strings[strings.length - 1].split("!")[0];
            File f = new File(leadPath);
            JarFile jarFile;
            try {
                jarFile = new JarFile(f);
                Enumeration<JarEntry> enumeration = jarFile.entries();
                while (enumeration.hasMoreElements()) {
                    JarEntry entry = enumeration.nextElement();
                    String s = entry.getName();
                    if (s != null && s.startsWith(path) && s.endsWith(".json")) {
                        files.add(new File(loader.getResource(s).getPath()));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            File packFile = FMLCommonHandler.instance().findContainerFor(AluminiumMod.aluminiumInstance).getSource();
            File newFile = new File(packFile.toURI().getPath() + path);
            files = Arrays.asList(newFile.listFiles());
        }
        return files;
    }

    //ItemStackを使ったマップを定義する。
    public static class ItemStackHashMap<K extends ItemStack, V extends List<ResourceLocation>> extends HashMap<K, V> {

        @Override
        public V get(Object key) {
            if (key instanceof ItemStack && this.containsKey(key)) {
                for (Map.Entry<K, V> entry : this.entrySet()) {
                    if (entry.getKey().getItem() == ((ItemStack) key).getItem() && entry.getKey().getMetadata() == ((ItemStack) key).getMetadata()) {
                        return entry.getValue();
                    }
                }
            }
            return null;
        }

        @Override
        public boolean containsKey(Object key) {
            if (key instanceof ItemStack) {
                ItemStack itemStack = (ItemStack) key;
                for (ItemStack stack : this.keySet()) {
                    if (stack.getItem() == itemStack.getItem() && stack.getMetadata() == itemStack.getMetadata()) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
