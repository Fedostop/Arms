package dev.melonity;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.input.CharInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class MelonityScreen extends Screen {
    private static final int GREEN = 0xFF9DFF45, BG = 0xF0101311;
    private static final int SURFACE = 0xFF171C18, HOVER = 0xFF222B23, MUTED = 0xFF89948B;
    private static final String[] CATEGORIES = {"Combat", "Movement", "Render", "Player", "Misc", "HUD"};
    private final List<ModuleEntry> modules = new ArrayList<>();
    private int category = 2, scroll, px, py, pw, ph;
    private String selected = "Interface", search = "";
    private boolean searchFocused;

    public MelonityScreen() { super(Text.literal("Melonity Client")); seed(); }

    private void seed() {
        modules.clear();
        switch (category) {
            case 0 -> { add("Totem Counter","Counts totems in your inventory","Display"); add("Hit Sounds","Hit confirmation audio","Audio"); add("Crosshair","Crosshair appearance","Visual"); add("Target Info","Current target information","HUD"); }
            case 1 -> { add("Sprint","Automatically holds sprint","Movement"); add("Motion Info","Movement speed and direction","HUD"); add("Elytra Info","Elytra flight information","HUD"); }
            case 2 -> { add("Interface","Client theme and accent options","Client"); add("Coordinates","Current world coordinates","HUD"); add("FPS Counter","Current frame rate","HUD"); add("Armor HUD","Equipped armor display","HUD"); add("Potion HUD","Active status effects","HUD"); add("Crosshair","Custom crosshair appearance","Visual"); add("Item Tags","Names above dropped items","Visual"); }
            case 3 -> { add("Auto Respawn","Respawn after death","Player"); add("Inventory View","Compact inventory information","HUD"); add("Totem Counter","Counts inventory totems","Display"); }
            case 4 -> { add("Notifications","Client notification center","Client"); add("Config Manager","Configuration profiles","Client"); add("ClickGUI","Menu appearance and behavior","Client"); }
            default -> { add("Watermark","Client name and version","HUD"); add("Array List","Enabled module list","HUD"); add("Target HUD","Target health and equipment","HUD"); add("Keybinds","Active module keybinds","HUD"); }
        }
        selected = modules.isEmpty() ? "" : modules.get(0).name;
        scroll = 0;
    }
    private void add(String n,String d,String g) { modules.add(new ModuleEntry(n,d,g)); }

    @Override public void render(DrawContext c,int mx,int my,float delta) {
        pw=Math.max(300,Math.min(760,width-24)); ph=Math.max(260,Math.min(470,height-24));
        px=(width-pw)/2; py=(height-ph)/2;
        c.fill(0,0,width,height,0x99000000);
        c.fill(px,py,px+pw,py+ph,BG); c.fill(px,py,px+3,py+ph,GREEN);
        c.fill(px+1,py+1,px+pw-1,py+62,0xFF151A16);
        c.fill(px+1,py+61,px+pw-1,py+62,0xFF303A32);
        c.fill(px+17,py+13,px+53,py+49,GREEN);
        c.drawCenteredTextWithShadow(textRenderer,"M",px+35,py+23,0xFF101510);
        c.drawText(textRenderer,"MELONITY",px+64,py+16,0xFFFFFFFF,true);
        c.drawText(textRenderer,"CLIENT  /  1.21.11",px+64,py+33,MUTED,false);
        c.drawText(textRenderer,"RIGHT SHIFT",px+pw-92,py+25,GREEN,false);

        int nx=px+14, ny=py+76, nw=Math.min(148,pw/4);
        c.fill(nx,ny,nx+nw,py+ph-14,0xFF141915);
        c.drawText(textRenderer,"CATEGORIES",nx+10,ny+10,MUTED,false);
        for(int i=0;i<CATEGORIES.length;i++){
            int y=ny+32+i*39; boolean active=i==category;
            if(active){c.fill(nx+5,y-6,nx+nw-5,y+24,0xFF263525);c.fill(nx+5,y-6,nx+8,y+24,GREEN);}
            c.drawText(textRenderer,CATEGORIES[i],nx+15,y+3,active?GREEN:0xFFB6BDB7,false);
        }

        int x=nx+nw+16, right=px+pw-14, available=right-x;
        int settingsW=Math.min(206,Math.max(0,available/3)), listW=available-settingsW-12;
        int top=py+78;
        c.drawText(textRenderer,CATEGORIES[category].toUpperCase(Locale.ROOT),x,top,GREEN,false);
        c.drawText(textRenderer,"MODULES  /  "+modules.size(),x,top+15,MUTED,false);
        int searchY=top+34;
        c.fill(x,searchY,x+listW,searchY+25,SURFACE);
        c.drawText(textRenderer,search.isEmpty()?"Search modules...":search,x+9,searchY+8,search.isEmpty()?MUTED:0xFFFFFFFF,false);
        if(searchFocused)c.fill(x+2,searchY+23,x+listW-2,searchY+25,GREEN);

        int cardY=searchY+33, bottom=py+ph-16, index=0;
        for(ModuleEntry m:filtered()){
            int y=cardY+index++*43-scroll*43;
            if(y+37<cardY||y>bottom)continue;
            boolean over=mx>=x&&mx<=x+listW&&my>=y&&my<=y+37;
            c.fill(x,y,x+listW,y+37,m.name.equals(selected)||over?HOVER:SURFACE);
            c.drawText(textRenderer,m.name,x+9,y+6,0xFFFFFFFF,false);
            c.drawText(textRenderer,m.description,x+9,y+20,MUTED,false);
            int tx=x+listW-34;
            c.fill(tx,y+11,tx+24,y+25,m.enabled?GREEN:0xFF465047);
            c.fill(tx+(m.enabled?14:2),y+13,tx+(m.enabled?22:10),y+23,0xFFFFFFFF);
        }
        int sx=x+listW+12;
        c.fill(sx,top,right,bottom,0xFF141915);
        c.drawText(textRenderer,"SETTINGS",sx+11,top+11,GREEN,false);
        ModuleEntry m=findSelected();
        c.drawText(textRenderer,m==null?"Select a module":m.name,sx+11,top+34,0xFFFFFFFF,false);
        if(m!=null){
            wrap(c,m.description,sx+11,top+53,settingsW-22,MUTED);
            c.drawText(textRenderer,"CATEGORY",sx+11,top+105,MUTED,false);
            c.drawText(textRenderer,m.group,sx+11,top+119,0xFFFFFFFF,false);
            c.drawText(textRenderer,"STATE",sx+11,top+148,MUTED,false);
            c.drawText(textRenderer,m.enabled?"ENABLED":"DISABLED",sx+11,top+162,m.enabled?GREEN:0xFFB6BDB7,false);
            c.drawText(textRenderer,"Toggle using the switch",sx+11,top+190,MUTED,false);
        }
        c.drawText(textRenderer,"0.1.1  /  FABRIC",sx+11,bottom-16,MUTED,false);
        super.render(c,mx,my,delta);
    }

    private void wrap(DrawContext c,String value,int x,int y,int max,int color){
        StringBuilder line=new StringBuilder();
        for(String word:value.split(" ")){
            String next=line.isEmpty()?word:line+" "+word;
            if(textRenderer.getWidth(next)>max&&!line.isEmpty()){
                c.drawText(textRenderer,line.toString(),x,y,color,false);y+=12;line.setLength(0);
            }
            if(!line.isEmpty())line.append(' ');line.append(word);
        }
        if(!line.isEmpty())c.drawText(textRenderer,line.toString(),x,y,color,false);
    }
    private List<ModuleEntry> filtered(){
        if(search.isBlank())return modules;
        String q=search.toLowerCase(Locale.ROOT);
        return modules.stream().filter(m->m.name.toLowerCase(Locale.ROOT).contains(q)||m.description.toLowerCase(Locale.ROOT).contains(q)).toList();
    }
    private ModuleEntry findSelected(){return modules.stream().filter(m->m.name.equals(selected)).findFirst().orElse(null);}

    @Override public boolean mouseClicked(Click click,boolean doubled){
        if(click.buttonInfo().button()!=0)return super.mouseClicked(click,doubled);
        double mx=click.x(),my=click.y();int nx=px+14,ny=py+76,nw=Math.min(148,pw/4);
        for(int i=0;i<CATEGORIES.length;i++){
            int y=ny+32+i*39;
            if(hit(mx,my,nx+5,y-6,nx+nw-5,y+24)){category=i;seed();search="";return true;}
        }
        int x=nx+nw+16,right=px+pw-14,available=right-x;
        int settingsW=Math.min(206,Math.max(0,available/3)),listW=available-settingsW-12;
        int sy=py+78+34;searchFocused=hit(mx,my,x,sy,x+listW,sy+25);
        int cy=sy+33,index=0;
        for(ModuleEntry m:filtered()){
            int y=cy+index++*43-scroll*43;
            if(hit(mx,my,x,y,x+listW,y+37)){selected=m.name;if(mx>=x+listW-42)m.enabled=!m.enabled;return true;}
        }
        return super.mouseClicked(click,doubled);
    }
    @Override public boolean keyPressed(KeyInput input){
        if(searchFocused&&input.key()==GLFW.GLFW_KEY_BACKSPACE&&!search.isEmpty()){search=search.substring(0,search.length()-1);scroll=0;return true;}
        if(input.key()==GLFW.GLFW_KEY_ESCAPE){if(searchFocused){searchFocused=false;return true;}close();return true;}
        return super.keyPressed(input);
    }
    @Override public boolean charTyped(CharInput input){
        if(searchFocused&&!Character.isISOControl(input.codepoint())&&search.length()<32){search+=Character.toString(input.codepoint());scroll=0;return true;}
        return super.charTyped(input);
    }
    @Override public boolean mouseScrolled(double mx,double my,double horizontal,double vertical){
        scroll=Math.max(0,Math.min(Math.max(0,filtered().size()-1),scroll-(int)Math.signum(vertical)));return true;
    }
    private boolean hit(double x,double y,int x1,int y1,int x2,int y2){return x>=x1&&x<=x2&&y>=y1&&y<=y2;}
    @Override public boolean shouldPause(){return false;}

    private static final class ModuleEntry{
        final String name,description,group;boolean enabled;
        ModuleEntry(String n,String d,String g){name=n;description=d;group=g;}
    }
}
