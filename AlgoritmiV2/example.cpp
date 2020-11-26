#include <iostream>
#include "Splaytree.cpp"


int main(){
    Tree<int>* albero = new Splaytree<int>();
    std::cout<<"splaytree"<<std::endl;
    albero->Insert(1)->Insert(2)->Insert(3)->Insert(4)->Insert(-1)->Insert(20)->Insert(17)->Insert(7);
    albero->printBT();
    return 0;
}
