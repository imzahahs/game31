package game31.app.friends;

import game31.model.FriendsAppModel;
import sengine.graphics2d.Sprite;
import sengine.mass.MassSerializable;

/**
 * Created by Azmi on 6/8/2017.
 */

public class FriendsUser implements MassSerializable {

    public final String name;
    public final String fullName;
    public final Sprite profile;
    public final String banner;
    public final String description;

    public FriendsUser(FriendsAppModel.ProfileModel model) {
        name = model.name;
        fullName = model.fullName;
        profile = Sprite.load(model.profile);
        banner = model.banner;
        description = model.description;

    }

    @MassConstructor
    public FriendsUser(String name, String fullName, Sprite profile, String banner, String description) {
        this.name = name;
        this.fullName = fullName;
        this.profile = profile;
        this.banner = banner;
        this.description = description;
    }

    @Override
    public Object[] mass() {
        return new Object[] { name, fullName, profile, banner, description };
    }
}
