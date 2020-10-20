
template <class T>
struct Node//node declaration
{
    T key;  
    Node* left;
    Node* right;
};

template <class T>
class Tree{
    protected Node<T>* root;
    public:
        virtual void Insert(T element) = 0;
        virtual bool Delete(T element) = 0;
        virtual Node<T>* Search(T element) = 0;
};