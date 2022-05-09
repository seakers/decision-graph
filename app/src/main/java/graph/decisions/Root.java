package graph.decisions;

import graph.Decision;
import graph.Graph;
import org.neo4j.driver.Record;

public class Root extends Decision {

    public static class Builder extends Decision.Builder<Root.Builder>{

        public Builder(Record node) {
            super(node);
        }

        public Root build() { return new Root(this); }
    }

    protected Root(Root.Builder builder){
        super(builder);
    }




//     _____                    _                     _____              _
//    |  __ \                  | |                   |  __ \            (_)
//    | |__) | __ _  _ __    __| |  ___   _ __ ___   | |  | |  ___  ___  _   __ _  _ __
//    |  _  / / _` || '_ \  / _` | / _ \ | '_ ` _ \  | |  | | / _ \/ __|| | / _` || '_ \
//    | | \ \| (_| || | | || (_| || (_) || | | | | | | |__| ||  __/\__ \| || (_| || | | |
//    |_|  \_\\__,_||_| |_| \__,_| \___/ |_| |_| |_| |_____/  \___||___/|_| \__, ||_| |_|
//                                                                           __/ |
//                                                                          |___/

    @Override
    public void generateRandomDesign() throws Exception{

    }




//      _____                                                 _____              _
//     / ____|                                               |  __ \            (_)
//    | |      _ __  ___   ___  ___   ___ __   __ ___  _ __  | |  | |  ___  ___  _   __ _  _ __   ___
//    | |     | '__|/ _ \ / __|/ __| / _ \\ \ / // _ \| '__| | |  | | / _ \/ __|| | / _` || '_ \ / __|
//    | |____ | |  | (_) |\__ \\__ \| (_) |\ V /|  __/| |    | |__| ||  __/\__ \| || (_| || | | |\__ \
//     \_____||_|   \___/ |___/|___/ \___/  \_/  \___||_|    |_____/  \___||___/|_| \__, ||_| |_||___/
//                                                                                   __/ |
//                                                                                  |___/

    @Override
    public void crossoverDesigns(int papa, int mama, double mutation_probability) throws Exception{

    }


//     ______                                             _    _
//    |  ____|                                           | |  (_)
//    | |__    _ __   _   _  _ __ ___    ___  _ __  __ _ | |_  _   ___   _ __
//    |  __|  | '_ \ | | | || '_ ` _ \  / _ \| '__|/ _` || __|| | / _ \ | '_ \
//    | |____ | | | || |_| || | | | | ||  __/| |  | (_| || |_ | || (_) || | | |
//    |______||_| |_| \__,_||_| |_| |_| \___||_|   \__,_| \__||_| \___/ |_| |_|


    @Override
    public void enumerateDesignSpace(){

    }







}
