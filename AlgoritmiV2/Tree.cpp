#include <iostream>

template <class T>
struct Node//node declaration
{
    T key;
    Node<T> *left,*right,*prec;
    Node() : left(nullptr),right(nullptr),prec(nullptr) {}
    Node(const T& _key) :key(_key),left(nullptr),right(nullptr),prec(nullptr){}
    // stack-abusing recursion everywhere, for small code
    //~Node() { delete left; delete right; delete prec;}
    int max_depth() const {
        const int left_depth = left ? left->max_depth() : 0;
        const int right_depth = right ? right->max_depth() : 0;
        return (left_depth > right_depth ? left_depth : right_depth) + 1;
    }
};

template <class T>
class Tree{
    protected:
        Node<T>* root;
    public:
        Node<T>* getroot(){return root;}
        T getprec(T el){
			Node<T>* element=_search(el),*pre;
			if(element){
				pre=_prec(element);
				if(pre)return pre->elem;
			}
			return -1;
		}
		T getsucc(T el){
			Node<T>* element=_search(el),*suc;
			if(element){
				suc=_succ(element);
				if(suc)return suc->elem;
			}
			return -1;
		}

        void printBT(const std::string& prefix, const Node<T>* node, bool isLeft)
        {
            if( node != nullptr )
            {
                std::cout << prefix;

                std::cout << (isLeft ? "├──" : "└──" );

                // print the value of the node
                std::cout << node->key << std::endl;

                // enter the next tree level - left and right branch
                printBT( prefix + (isLeft ? "│   " : "    "), node->left, true);
                printBT( prefix + (isLeft ? "│   " : "    "), node->right, false);
            }
        }

        void printBT()
        {
            printBT("", root, false);    
        }

        virtual Tree<T>* Insert(T element) = 0;
        virtual Tree<T>* Delete(T element) = 0;
        virtual Node<T>* Search(T element) = 0;
};