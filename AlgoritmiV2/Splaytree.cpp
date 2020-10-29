#include <iostream>
#include <cstdio>
#include <cstdlib>
#include "Tree.cpp"

template<class T>
class Splaytree:public Tree<T>{
   //normal methods
   protected:
      Node<T>* _min(Node<T>* p){
         while(p && p->left){
               p=p->left;
         }
         return p;
      }
      Node<T>* _max(Node<T>* p){
         while(p && p->right){
               p=p->right;
         }
         return p;
      }
      Node<T>* _succ(Node<T>* p){
         if(p && p->right){
               return _min(p->right);
         }
         Node<T>* y=p->prec;
         while(y && y->right=p){
               p=y;
               y=y->prec;
         }
         return y;
      }
      Node<T>* _prec(Node<T>* p){
         if(p && p->left){
               return _min(p->left);
         }
         Node<T>* y=p->prec;
         while(y && y->left=p){
               p=y;
               y=y->prec;
         }
         return y;
      }
      Node<T>* _search(T el){
         Node<T>* iter=this->root;
         while(iter && iter->key!=el){
               if(el>iter->key)iter=iter->right;
               else iter=iter->left;
         }
      }
      void transplant(Node<T>*z,Node<T>*y){
         if(!z->prec)this->root=y;
         else if(z->prec->right==z)z->prec->right=y;
         else if(z->prec->left==z)z->prec->left=y;
         if(y)y->prec=z->prec;
      }
   


      //splay tree auxiliary methods
      Node<T>* RR_Rotate(Node<T>* k2)
      {
         Node<T>* k1 = k2->left;
         k2->left = k1->right;
         k1->right = k2;
         return k1;
      }
      Node<T>* LL_Rotate(Node<T>* k2)
      {
         Node<T>* k1 = k2->right;
         k2->right = k1->left;
         k1->left = k2;
         return k1;
      }
      Node<T> *Splay(T key, Node<T> *_root)
      {
         if (!_root)
         return NULL;
         Node<T> header;
         header.left= header.right = NULL;
         Node<T>* LeftTreeMax = &header;
         Node<T>* RightTreeMin = &header;
         while (1)
         {
            if (key < _root->key)
            {
               if (!_root->left)
               break;
               if (key< _root->left->key)
               {
                  _root = RR_Rotate(_root);
                  if (!_root->left)
                  break;
               }
               RightTreeMin->left= _root;
               RightTreeMin = RightTreeMin->left;
               _root = _root->left;
               RightTreeMin->left = NULL;
            }
            else if (key> _root->key)
            {
               if (!_root->right)
               break;
               if (key > _root->right->key)
               {
                  _root = LL_Rotate(_root);
                  if (!_root->right)
                  break;
               }
               LeftTreeMax->right= _root;
               LeftTreeMax = LeftTreeMax->right;
               _root = _root->right;
               LeftTreeMax->right = NULL;
            }
            else
            break;
         }
         LeftTreeMax->right = _root->left;
         RightTreeMin->left = _root->right;
         _root->left = header.right;
         _root->right = header.left;
         return _root;
      }
      Node<T>* New_Node(T key)
      {
         Node<T>* p_node = new Node<T>();
         if (!p_node)
         {
            std::cerr<< "Out of memory!\n"<<std::endl;
            exit(1);
         }
         p_node->key = key;
         p_node->left = p_node->right = NULL;
         return p_node;
      }

   public:
      Splaytree<T>* Insert(T element) final{
         Node<T>* y=NULL,*x=this->root;
            if(!this->root){
                this->root=new Node<T>(element);
                return this;
            }
            while(x){
                y=x;
                if(element<=x->key)x=x->left;
                else x=x->right;
            }
            x=new Node<T>(element);
            x->prec=y;
            if(!y)this->root=x;
            else if(element<=y->key)y->left=x;
            else y->right=x;
            Splay(y->key,this->getroot());
            return this;         
      }
      void remove(Node<T>* z){
            if(z){
                if(!z->left)transplant(z,z->right);
                else if(!z->right)transplant(z,z->left);
                else{
                    Node<T>* y=_min(z->right);
                    if(y!=z->right){
                        transplant(y,y->right);
                        y->right=z->right;
                        y->right->prec=y;
                    }
                    transplant(z,y);
                    y->left=z->left;
                    y->left->prec=y;
                }
                delete z;
            }
        }  
        Splaytree<T>* Delete(T el) final{
            Node<T>* z=Search(el);
            remove(z);
            return this;
        } 
      Node<T>* Search(T element) final{
         if(_search(element)!=nullptr)Splay(element,this->getroot());
         return _search(element);
      }
      

      


};